package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 size in PostScript points (72 points/inch)
    private const val PAGE_HEIGHT = 842 // A4 size in PostScript points

    /**
     * Draws the beautiful military helicopter seal emblem at top of pages.
     */
    private fun drawEmblem(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 1. Deep forest green background
        paint.color = 0xFF0F5132.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.5f, paint)

        // 2. Bright gold/yellow accent ring
        paint.color = 0xFFFBBF24.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.04f
        canvas.drawCircle(cx, cy, size * 0.46f, paint)

        // 3. Helicopter silhouette elements in white
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        
        // Fuselage oval
        canvas.drawOval(
            cx - size * 0.22f, cy - size * 0.08f,
            cx + size * 0.22f, cy + size * 0.08f,
            paint
        )

        // Tail boom
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.04f
        canvas.drawLine(cx - size * 0.18f, cy, cx - size * 0.45f, cy - size * 0.04f, paint)

        // Rotor Mast
        paint.strokeWidth = size * 0.025f
        canvas.drawLine(cx, cy - size * 0.08f, cx, cy - size * 0.22f, paint)

        // Main Rotor Blade
        paint.strokeWidth = size * 0.02f
        canvas.drawLine(cx - size * 0.42f, cy - size * 0.22f, cx + size * 0.42f, cy - size * 0.22f, paint)

        // Skids
        paint.strokeWidth = size * 0.025f
        canvas.drawLine(cx - size * 0.16f, cy + size * 0.15f, cx + size * 0.20f, cy + size * 0.15f, paint)
        canvas.drawLine(cx - size * 0.10f, cy + size * 0.08f, cx - size * 0.10f, cy + size * 0.15f, paint)
        canvas.drawLine(cx + size * 0.10f, cy + size * 0.08f, cx + size * 0.10f, cy + size * 0.15f, paint)
    }

    /**
     * Text wrapping drawer helper.
     */
    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        textPaint: TextPaint
    ): Int {
        canvas.save()
        canvas.translate(x, y)
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
        }
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height
    }

    /**
     * Formats Timestamp to local date.
     */
    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm น.", Locale("th", "TH"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Exports a comprehensive PDF summary report of all passed records.
     */
    fun exportRecordListToPdf(
        context: Context,
        records: List<AviationRecord>,
        filterDescription: String
    ): File? {
        val pdfDocument = PdfDocument()
        val textPaint = TextPaint().apply { isAntiAlias = true }
        val paint = Paint().apply { isAntiAlias = true }

        // Filter and document count statistics
        val countWO = records.count { it.type == "WO" }
        val countWS = records.count { it.type == "WS" }
        val countWB = records.count { it.type == "WB" }
        val countSHR = records.count { it.type == "SHR" }
        val countCRS = records.count { it.type == "CRS" }

        // Pagination setup
        val recordsPerPage = 12
        val totalPages = Math.max(1, (records.size + recordsPerPage - 1) / recordsPerPage)

        for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = 40f

            // --- PAGE HEADER BLOCK ---
            // Draw helicopter emblem on left
            drawEmblem(canvas, 60f, currentY + 35f, 50f)

            // Header titles
            textPaint.apply {
                color = 0xFF0F5132.toInt() // Forest Green theme
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("กองทัพบก - ร้อย.ซบร.บ.ทบ.สท.", 100f, currentY + 20f, textPaint)

            textPaint.apply {
                color = 0xFF1E293B.toInt()
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ระบบฐานข้อมูลบริหารงานซ่อมบำรุงอากาศยาน (AMS Report)", 100f, currentY + 38f, textPaint)

            textPaint.apply {
                color = 0xFF64748B.toInt()
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("รายงานสรุปประวัติงานซ่อมบำรุงดิจิทัลเพื่อการตรวจสอบทางบริหาร", 100f, currentY + 52f, textPaint)

            // Line separation
            paint.color = 0xFFCBD5E1.toInt()
            paint.strokeWidth = 1.5f
            canvas.drawLine(30f, currentY + 68f, PAGE_WIDTH - 30f, currentY + 68f, paint)

            currentY += 80f

            // On the FIRST page, display statistical charts/boxes
            if (pageIndex == 0) {
                // Background summary box
                paint.color = 0xFFF1F5F9.toInt()
                paint.style = Paint.Style.FILL
                canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 45f, paint)

                paint.color = 0xFF0F5132.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 45f, paint)

                // Summarized counters
                textPaint.apply {
                    color = 0xFF1E293B.toInt()
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("สรุปข้อมูลเอกสารทั้งหมด: ${records.size} ฉบับ   |   ฟิลเตอร์: $filterDescription", 40f, currentY + 16f, textPaint)

                textPaint.apply {
                    color = 0xFF334155.toInt()
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText("ใบสั่งงาน (WO): $countWO   •   ใบสั่งทำ (WS): $countWS   •   ใบสรุปงาน (WB): $countWB   •   ส่งมอบผลัด (SHR): $countSHR   •   ปล่อยอากาศยาน (CRS): $countCRS", 40f, currentY + 32f, textPaint)

                currentY += 60f
            }

            // --- TABLE DRAWING ---
            // Draw Table Headers
            paint.color = 0xFF0F5132.toInt()
            paint.style = Paint.Style.FILL
            canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 20f, paint)

            textPaint.apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ลำดับ", 35f, currentY + 13f, textPaint)
            canvas.drawText("ประเภท", 70f, currentY + 13f, textPaint)
            canvas.drawText("เลขที่อ้างอิง (Ref No.)", 115f, currentY + 13f, textPaint)
            canvas.drawText("รายละเอียด / หัวข้อประวัติงานซ่อมบำรุง", 220f, currentY + 13f, textPaint)
            canvas.drawText("วันที่ฟอร์ม", PAGE_WIDTH - 110f, currentY + 13f, textPaint)

            currentY += 20f

            // Table rows loop
            val startRecordIndex = pageIndex * recordsPerPage
            val endRecordIndex = Math.min(startRecordIndex + recordsPerPage, records.size)

            paint.style = Paint.Style.FILL
            textPaint.color = 0xFF1E293B.toInt()
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            for (i in startRecordIndex until endRecordIndex) {
                val record = records[i]

                // Alternating row backgrounds
                if (i % 2 == 0) {
                    paint.color = 0xFFF8FAFC.toInt()
                } else {
                    paint.color = Color.WHITE
                }
                canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 24f, paint)

                // Row bottom border
                paint.color = 0xFFE2E8F0.toInt()
                paint.strokeWidth = 0.5f
                canvas.drawLine(30f, currentY + 24f, PAGE_WIDTH - 30f, currentY + 24f, paint)

                // Text labels
                textPaint.textSize = 8.5f
                canvas.drawText("${i + 1}", 35f, currentY + 15f, textPaint)

                // Draw colored tag for type
                val tagColor = when (record.type) {
                    "WO" -> 0xFF2563EB.toInt() // Blue
                    "WS" -> 0xFF0D9488.toInt() // Teal
                    "WB" -> 0xFFEA580C.toInt() // Orange
                    "SHR" -> 0xFF7C3AED.toInt() // Purple
                    else -> 0xFF16A34A.toInt() // Green
                }
                paint.color = tagColor
                canvas.drawRoundRect(65f, currentY + 4f, 105f, currentY + 18f, 4f, 4f, paint)
                
                val oldColor = textPaint.color
                textPaint.color = Color.WHITE
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(record.type, 74f, currentY + 13f, textPaint)
                
                textPaint.color = oldColor
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(record.referenceNo, 115f, currentY + 15f, textPaint)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                
                // Crop description text so it doesn't overlap
                val shortTitle = if (record.title.length > 45) {
                    record.title.take(43) + "..."
                } else record.title
                canvas.drawText(shortTitle, 220f, currentY + 15f, textPaint)

                val dateLabel = record.date.ifBlank { "N/A" }
                canvas.drawText(dateLabel, PAGE_WIDTH - 110f, currentY + 15f, textPaint)

                currentY += 24f
            }

            // --- PAGE FOOTER & SIGN-OFF ---
            // If on the LAST page, draw the formal signature block
            if (pageIndex == totalPages - 1) {
                currentY += 35f

                // Border Box for review signoff
                paint.color = 0xFFF1F5F9.toInt()
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(250f, currentY, PAGE_WIDTH - 30f, currentY + 95f, 6f, 6f, paint)

                paint.color = 0xFFCBD5E1.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(250f, currentY, PAGE_WIDTH - 30f, currentY + 95f, 6f, 6f, paint)

                textPaint.apply {
                    color = 0xFF334155.toInt()
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("ส่วนอนุมัติและรับรองรายงานการซ่อมบำรุง", 270f, currentY + 18f, textPaint)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("ลงชื่อ............................................................ ผู้รับรองประวัติ", 270f, currentY + 44f, textPaint)
                canvas.drawText("ตำแหน่ง......................................................... นายทหารฝ่ายซ่อมบำรุง", 270f, currentY + 64f, textPaint)
                canvas.drawText("วันที่........./........./................", 270f, currentY + 82f, textPaint)
            }

            // Draw Page Number & System Stamp at very bottom
            paint.color = 0xFFCBD5E1.toInt()
            paint.strokeWidth = 0.5f
            canvas.drawLine(30f, PAGE_HEIGHT - 35f, PAGE_WIDTH - 30f, PAGE_HEIGHT - 35f, paint)

            textPaint.apply {
                color = 0xFF94A3B8.toInt()
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("บันทึกประวัติการส่งออกแบบดิจิทัล ณ วันที่: ${formatTimestamp(System.currentTimeMillis())}", 30f, PAGE_HEIGHT - 22f, textPaint)
            canvas.drawText("หน้า ${pageIndex + 1} จาก $totalPages", PAGE_WIDTH - 85f, PAGE_HEIGHT - 22f, textPaint)

            pdfDocument.finishPage(page)
        }

        // Save generated PDF to App cache directory for quick sharing
        return try {
            val fileName = "AMS_Maintenance_Report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exports a detailed 1-page official military administrative form layout for a single record.
     */
    fun exportSingleRecordToPdf(context: Context, record: AviationRecord): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val textPaint = TextPaint().apply { isAntiAlias = true }
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 40f

        // --- SECTION 1: OFFICIAL MILITARY HEADER ---
        drawEmblem(canvas, 60f, currentY + 35f, 55f)

        textPaint.apply {
            color = 0xFF0F5132.toInt() // Deep Military Green
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("ร้อย.ซบร.บ.ทบ.สท. (กองทัพบก)", 105f, currentY + 18f, textPaint)

        textPaint.apply {
            color = 0xFF1E293B.toInt()
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val fullTypeName = getFormTypeName(record.type)
        canvas.drawText("ใบสถิติงานและบันทึกข้อมูลหลัก: $fullTypeName", 105f, currentY + 36f, textPaint)

        textPaint.apply {
            color = 0xFF475569.toInt()
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("ระบบฐานข้อมูลดิจิทัลบริหารการซ่อมบำรุง (Aviation Maintenance Form)", 105f, currentY + 52f, textPaint)

        // Line sep
        paint.color = 0xFF0F5132.toInt()
        paint.strokeWidth = 2f
        canvas.drawLine(30f, currentY + 74f, PAGE_WIDTH - 30f, currentY + 74f, paint)

        currentY += 86f

        // --- SECTION 2: SYSTEM INFORMATION BOX ---
        paint.color = 0xFFF8FAFC.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 55f, paint)

        paint.color = 0xFFCBD5E1.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 55f, paint)

        // Info entries
        textPaint.apply {
            color = 0xFF1E293B.toInt()
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("รหัสใบงานซ่อมบำรุง:", 40f, currentY + 18f, textPaint)
        canvas.drawText("เลขที่เอกสารอ้างอิงหลัก:", 40f, currentY + 34f, textPaint)
        canvas.drawText("วันที่ลงระบบฐานข้อมูล:", 40f, currentY + 48f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${record.type} - $fullTypeName", 150f, currentY + 18f, textPaint)
        canvas.drawText(record.referenceNo, 150f, currentY + 34f, textPaint)
        canvas.drawText(formatTimestamp(record.savedAt), 150f, currentY + 48f, textPaint)

        // Draw huge watermark/badge in top-right of system info
        paint.color = when (record.type) {
            "WO" -> 0xFF2563EB.toInt()
            "WS" -> 0xFF0D9488.toInt()
            "WB" -> 0xFFEA580C.toInt()
            "SHR" -> 0xFF7C3AED.toInt()
            else -> 0xFF16A34A.toInt()
        }
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(PAGE_WIDTH - 120f, currentY + 12f, PAGE_WIDTH - 40f, currentY + 42f, 5f, 5f, paint)

        val oldTextColor = textPaint.color
        textPaint.apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(record.type, PAGE_WIDTH - 95f, currentY + 31f, textPaint)

        textPaint.color = oldTextColor
        currentY += 72f

        // --- SECTION 3: DETAILED ATTRIBUTES PARSING ---
        textPaint.apply {
            color = 0xFF0F5132.toInt()
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("รายละเอียดข้อมูลบันทึกทางเทคนิค (Parsed Attributes Log)", 30f, currentY, textPaint)
        currentY += 10f

        // Draw Table Grid for attributes
        val attributeData = ArrayList<Pair<String, String>>()
        when (record.type) {
            "WO" -> {
                val data = AviationSerializer.toWo(record.jsonContent)
                attributeData.add("เลขที่ใบส่งซ่อม" to data.repairNo)
                attributeData.add("WO/ON (เลขงาน)" to data.woOn)
                attributeData.add("วันที่ส่งซ่อม" to data.repairDate)
                attributeData.add("ลงวันที่" to data.docDate)
                attributeData.add("สายยุทโธปกรณ์" to data.equipmentLine)
                attributeData.add("P/N (Part Number)" to data.partNo)
                attributeData.add("S/N (Serial Number)" to data.serialNo)
                attributeData.add("ชื่อชิ้นส่วนอุปกรณ์ (ITEM NAME)" to data.itemName)
                attributeData.add("ชั่วโมงบินสะสม (TSO / TSN)" to data.tsoTsn)
                attributeData.add("หน่วยซ่อมบำรุง (OWNER)" to data.owner)
                attributeData.add("วันที่ได้รับงานซ่อม" to data.receivedDate)
                attributeData.add("ตอนรับงานซ่อมบำรุง" to data.section)
                attributeData.add("สถานที่ซ่อมบำรุง (LOCATION)" to data.maintenanceLocation)
                attributeData.add("ผู้ตรวจสอบ / ผู้รับงาน" to data.receiver)
                attributeData.add("ลำดับความเร่งด่วนเอกสาร" to data.priority)
                attributeData.add("รายละเอียดงานซ่อม (WORK DETAILS)" to data.workDetails)
                attributeData.add("เอกสารอ้างอิง (REF DOCUMENTS)" to data.refDocs)
            }
            "WS" -> {
                val data = AviationSerializer.toWs(record.jsonContent)
                attributeData.add("เลขที่รหัสคุม WS/ON" to data.wsOn)
                attributeData.add("เลขอ้างอิงใบงาน WO/ON Ref" to data.woOnRef)
                attributeData.add("วันที่กำหนดงาน (DATE)" to data.date)
                attributeData.add("รุ่นอากาศยาน (AIRCRAFT TYPE)" to data.aircraftType)
                attributeData.add("เลขหมายอากาศยาน (MSN)" to data.aircraftMsn)
                attributeData.add("คู่มือทางเทคนิคที่ปฏิบัติ" to data.technicalManual)
            }
            "WB" -> {
                val data = AviationSerializer.toWb(record.jsonContent)
                attributeData.add("เลขสรุปคุมงาน WB/ON" to data.wbOn)
                attributeData.add("เลขอ้างอิงใบสั่งทำ WS/ON Ref" to data.wsOnRef)
                attributeData.add("วันที่สรุปงาน" to data.date)
                attributeData.add("รายละเอียดงานซ่อมที่ซ่อมเสร็จ" to data.workTasks)
                attributeData.add("นายทหารผู้ควบคุม / SUPERVISER" to data.supervisor)
                attributeData.add("ช่างผู้ปฏิบัติการซ่อมบำรุง" to data.performer)
            }
            "SHR" -> {
                val data = AviationSerializer.toShr(record.jsonContent)
                attributeData.add("เลขควบคุมส่งผลัด SHR/ON" to data.shrOn)
                attributeData.add("เลขอ้างอิงใบสรุปงาน WB/ON Ref" to data.wbOnRef)
                attributeData.add("วันที่ส่งมอบผลัด" to data.date)
                attributeData.add("รุ่นอากาศยาน (AIRCRAFT TYPE)" to data.aircraftType)
                attributeData.add("เลขหมายเครื่อง MSN" to data.aircraftMsn)
                attributeData.add("โรงเก็บซ่อมบำรุง (LOCATION)" to data.maintenanceLocation)
                attributeData.add("ใบสั่งทำ (WORK SHEET REF)" to data.workPackageRef)
                attributeData.add("วันเวลาเริ่มส่งผลัด" to data.startDate)
                attributeData.add("เวลาสิ้นสุดผลัดซ่อมบำรุง" to data.shiftEnd)
                attributeData.add("เลขควบคุมงานสั่งซ่อม (TASK WO)" to data.taskWoNo)
                attributeData.add("ขั้นตอนล่าสุดที่เสร็จสิ้น" to data.lastStepCompleted)
                attributeData.add("ข้อควรระวัง / งานตกค้างส่งต่อ" to data.remainingCaution)
            }
            "CRS" -> {
                val data = AviationSerializer.toCrs(record.jsonContent)
                attributeData.add("ใบรับรองการปล่อยเครื่อง CRS/ON" to data.crsOn)
                attributeData.add("เลขอ้างอิงงานซ่อมหลัก WO/ON" to data.woOnRef)
                attributeData.add("แบบอากาศยาน (A/C TYPE)" to data.aircraftType)
                attributeData.add("หน่วยงานผู้เป็นเจ้าของ (OWNER)" to data.owner)
                attributeData.add("สถานที่ออกใบรับรองปล่อยบิน" to data.maintenanceLocation)
                attributeData.add("หมายเลขเครื่องบิน MSN" to data.aircraftMsn)
                attributeData.add("ชื่ออะไหล่/ชิ้นส่วน (ITEM NAME)" to data.itemName)
                attributeData.add("รหัสพาร์ทอะไหล่ P/N" to data.partNo)
                attributeData.add("ซีเรียลนัมเบอร์พาร์ท S/N" to data.serialNo)
                attributeData.add("รายละเอียดโดยละเอียดการปล่อย" to data.detailedDescription)
                attributeData.add("ผลการทดสอบการเป็นเอกเทศ" to data.independentInspection)
                attributeData.add("ข้อบกพร่องที่ผ่อนผันให้อนุโลม" to data.deferredDefects)
            }
        }

        // Draw professional boxed list
        paint.style = Paint.Style.STROKE
        paint.color = 0xFFCBD5E1.toInt()
        paint.strokeWidth = 1f

        textPaint.apply {
            color = 0xFF1E293B.toInt()
            textSize = 9f
        }

        for (item in attributeData) {
            // Background card fill
            paint.style = Paint.Style.FILL
            paint.color = 0xFFF8FAFC.toInt()
            canvas.drawRoundRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 32f, 4f, 4f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = 0xFFE2E8F0.toInt()
            canvas.drawRoundRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 32f, 4f, 4f, paint)

            // Draw field label in Bold
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = 0xFF475569.toInt()
            canvas.drawText(item.first, 40f, currentY + 14f, textPaint)

            // Draw field value with text wrap support in normal font
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = 0xFF0F172A.toInt()

            val wrappedText = if (item.second.isBlank()) "-" else item.second
            drawMultilineText(
                canvas = canvas,
                text = wrappedText,
                x = 180f,
                y = currentY + 6f,
                width = PAGE_WIDTH - 220,
                textPaint = textPaint
            )

            currentY += 36f

            // Break if we exceed standard vertical space limit to keep layout neat (1 page focus)
            if (currentY > PAGE_HEIGHT - 170f) {
                // If there are too many fields left, print a summary indicator
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textPaint.color = 0xFF64748B.toInt()
                canvas.drawText("[...แสดงข้อมูลครบถ้วนสมบูรณ์ในระบบเอกสารดิจิทัลหลัก...]", 30f, currentY + 12f, textPaint)
                currentY += 20f
                break
            }
        }

        // --- SECTION 4: ADMINISTRATIVE SIGN-OFF BLOCK ---
        currentY = PAGE_HEIGHT - 150f

        paint.color = 0xFFF1F5F9.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 95f, 6f, 6f, paint)

        paint.color = 0xFF0F5132.toInt() // Green border to signify approval
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(30f, currentY, PAGE_WIDTH - 30f, currentY + 95f, 6f, 6f, paint)

        textPaint.apply {
            color = 0xFF0F5132.toInt()
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("คณะกรรมการผู้ตรวจสอบซ่อมบำรุงและปล่อยอากาศยาน ร้อย.ซบร.บ.ทบ.สท.", 45f, currentY + 20f, textPaint)

        textPaint.apply {
            color = 0xFF334155.toInt()
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("ลงชื่อ.............................................................. นายทหารช่างตรวจรับงานซ่อม", 45f, currentY + 48f, textPaint)
        canvas.drawText("ลงชื่อ.............................................................. นักบินผู้ปล่อยบินรับรอง (CRS Sign)", 45f, currentY + 74f, textPaint)

        // Draw system stamp watermark
        paint.color = 0xFFCBD5E1.toInt()
        paint.strokeWidth = 0.5f
        canvas.drawLine(30f, PAGE_HEIGHT - 35f, PAGE_WIDTH - 30f, PAGE_HEIGHT - 35f, paint)

        textPaint.apply {
            color = 0xFF94A3B8.toInt()
            textSize = 7.5f
        }
        canvas.drawText("ระบบสารสนเทศความสมควรเดินอากาศ (AMS-Aero-RTAA) | เลขใบสถิติ: ${record.referenceNo}", 30f, PAGE_HEIGHT - 22f, textPaint)
        canvas.drawText("เอกสารสถิติดิจิทัล 1 หน้าสมบูรณ์", PAGE_WIDTH - 150f, PAGE_HEIGHT - 22f, textPaint)

        pdfDocument.finishPage(page)

        // Save generated single PDF
        return try {
            val fileName = "AMS_Form_${record.type}_${record.referenceNo}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Helper to launch standard sharing action popup on Android.
     */
    fun sharePdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, "ส่งออกรายงานบันทึกประวัติการปฏิบัติงานซ่อมบำรุงอากาศยาน ร้อย.ซบร.บ.ทบ.สท. (AMS Digital Report)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "แบ่งปันรายงาน PDF (Export PDF)"))
        } catch (e: Exception) {
            Toast.makeText(context, "เกิดข้อผิดพลาดในการแชร์ไฟล์: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getFormTypeName(type: String): String {
        return when (type) {
            "WO" -> "ใบสั่งงาน (Work Order)"
            "WS" -> "ใบสั่งทำ (Work Sheet)"
            "WB" -> "ใบสรุปงาน (Work Brief)"
            "SHR" -> "บันทึกการส่งมอบผลัด (Shift Handover)"
            "CRS" -> "ใบรับรองการปล่อยอากาศยาน (Release to Service)"
            else -> "แบบฟอร์ม"
        }
    }
}
