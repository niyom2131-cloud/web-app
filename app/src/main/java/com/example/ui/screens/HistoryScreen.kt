package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.viewmodel.AviationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: AviationViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val filteredRecords by viewModel.filteredRecords.collectAsState(initial = emptyList())
    val selectedViewRecord by viewModel.selectedViewRecord.collectAsState()
    val context = LocalContext.current

    // Delete confirm dialogue states
    var recordToDelete by remember { mutableStateOf<AviationRecord?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filters Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("ค้นหาเลขที่, หัวข้อ หรือวันที่...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            } else null,
            shape = androidx.compose.foundation.shape.CircleShape,
            singleLine = true
        )

        // Horizontal Category Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ประเภท:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Category list (All, WO, WS, WB, SHR, CRS)
            val filters = listOf(
                FilterItem(null, "ทั้งหมด"),
                FilterItem("WO", "WO"),
                FilterItem("WS", "WS"),
                FilterItem("WB", "WB"),
                FilterItem("SHR", "SHR"),
                FilterItem("CRS", "CRS")
            )

            Box(modifier = Modifier.weight(1f)) {
                ScrollableRow(
                    filters = filters,
                    selected = categoryFilter,
                    onSelect = { viewModel.setCategoryFilter(it) }
                )
            }
        }

        // List View / Content
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "ไม่พบข้อมูลที่ตรงกับคำค้นหา" else "ไม่มีข้อมูลที่บันทึกไว้ในระบบ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    HistoryItemCard(
                        record = record,
                        onClick = { viewModel.selectRecordToView(record) },
                        onDeleteClick = { recordToDelete = record }
                    )
                }
            }
        }
    }

    // Modal Sheet / Custom Dialog to View Record Details
    selectedViewRecord?.let { record ->
        RecordDetailsDialog(
            record = record,
            onDismiss = { viewModel.selectRecordToView(null) },
            context = context
        )
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ยืนยันการลบข้อมูล") },
            text = { Text("คุณต้องการลบแบบฟอร์ม ${record.type} เลขที่ ${record.referenceNo} ออกจากประวัติใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(record.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบข้อมูล")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

// Custom horizontal container
@Composable
fun ScrollableRow(
    filters: List<FilterItem>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { item ->
            val isSelected = item.tag == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item.tag) },
                label = { Text(item.label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

data class FilterItem(val tag: String?, val label: String)

@Composable
fun HistoryItemCard(
    record: AviationRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Document code Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (record.type) {
                            "WO" -> MaterialTheme.colorScheme.primaryContainer
                            "WS" -> Color(0xFFE0F7FA)
                            "WB" -> Color(0xFFFFF3E0)
                            "SHR" -> Color(0xFFEDE7F6)
                            else -> Color(0xFFE8F5E9)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.type,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (record.type) {
                        "WO" -> MaterialTheme.colorScheme.primary
                        "WS" -> Color(0xFF006064)
                        "WB" -> Color(0xFFE65100)
                        "SHR" -> Color(0xFF311B92)
                        else -> Color(0xFF1B5E20)
                    },
                    fontSize = 15.sp
                )
            }

            // Central info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${getFormTypeName(record.type)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ref/ON: ${record.referenceNo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action / Timestamp
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = record.date.ifBlank { "ไม่ลงวันที่" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------- DIALOG FOR VIEWING DETAILED FIELDS ----------------
@Composable
fun RecordDetailsDialog(
    record: AviationRecord,
    onDismiss: () -> Unit,
    context: Context
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔍 รายละเอียดแบบฟอร์ม (${record.type})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Reference / ON: ${record.referenceNo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }

                // Scrollable main content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "📌 ข้อมูลบันทึกประวัติการใช้",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LabelValueRow("วันที่บันทึก:", formatDateLong(record.savedAt))
                                LabelValueRow("ชนิดฟอร์ม:", "${record.type} - ${getFormTypeName(record.type)}")
                                LabelValueRow("ค่าคีย์อ้างอิงหลัก:", record.referenceNo)
                                LabelValueRow("หัวข้อ/จำแนก:", record.title)
                            }
                        }
                    }

                    // Fields parsing based on category
                    item {
                        Text(
                            text = "📝 รายละเอียดเชิงลึกใบสถิติ (Parsed Attributes)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    when (record.type) {
                        "WO" -> {
                            val data = AviationSerializer.toWo(record.jsonContent)
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DataRow("เลขที่ใบส่งซ่อม", data.repairNo)
                                    DataRow("WO/ON (เลขงาน)", data.woOn)
                                    DataRow("วันที่ส่งซ่อม", data.repairDate)
                                    DataRow("ลงวันที่", data.docDate)
                                    DataRow("สายยุทโธปกรณ์", data.equipmentLine)
                                    DataRow("P/N (Part Number)", data.partNo)
                                    DataRow("S/N (Serial Number)", data.serialNo)
                                    DataRow("ITEM NAME", data.itemName)
                                    DataRow("TSO / TSN", data.tsoTsn)
                                    DataRow("OWNER / หน่วยส่งซ่อม", data.owner)
                                    DataRow("วันที่รับงานซ่อม", data.receivedDate)
                                    DataRow("ตอนรับงานซ่อม", data.section)
                                    DataRow("MAINTENANCE LOCATION", data.maintenanceLocation)
                                    DataRow("ผู้รับงานซ่อม", data.receiver)
                                    DataRow("ลำดับความเร่งด่วน", data.priority)
                                    DataBlockRow("WORK DETAILS", data.workDetails)
                                    DataRow("REFERENCE DOCUMENTS", data.refDocs)
                                }
                            }
                        }
                        "WS" -> {
                            val data = AviationSerializer.toWs(record.jsonContent)
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DataRow("WS/ON", data.wsOn)
                                    DataRow("WO/ON Ref", data.woOnRef)
                                    DataRow("DATE", data.date)
                                    DataRow("AIRCRAFT TYPE", data.aircraftType)
                                    DataRow("AIRCRAFT MSN", data.aircraftMsn)
                                    DataBlockRow("OPERATION MAINTENANCE TECHNICAL MANUAL", data.technicalManual)
                                }
                            }
                        }
                        "WB" -> {
                            val data = AviationSerializer.toWb(record.jsonContent)
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DataRow("WB/ON", data.wbOn)
                                    DataRow("WS/ON Ref", data.wsOnRef)
                                    DataRow("DATE", data.date)
                                    DataBlockRow("WORK TASKS OPERATION", data.workTasks)
                                    DataRow("SUPERVISER / CONTROLLER", data.supervisor)
                                    DataRow("PERFORMER", data.performer)
                                }
                            }
                        }
                        "SHR" -> {
                            val data = AviationSerializer.toShr(record.jsonContent)
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DataRow("SHR/ON", data.shrOn)
                                    DataRow("WB/ON Ref", data.wbOnRef)
                                    DataRow("DATE", data.date)
                                    DataRow("AIRCRAFT TYPE", data.aircraftType)
                                    DataRow("AIRCRAFT MSN", data.aircraftMsn)
                                    DataRow("MAINTENANCE LOCATION", data.maintenanceLocation)
                                    DataRow("WORK PACKAGE REF: WORK SHEET", data.workPackageRef)
                                    DataRow("START DATE", data.startDate)
                                    DataRow("SHIFT END", data.shiftEnd)
                                    DataRow("TASK WO/NO", data.taskWoNo)
                                    DataRow("LAST STEP COMPLETED", data.lastStepCompleted)
                                    DataBlockRow("REMAINING / CAUTION", data.remainingCaution)
                                }
                            }
                        }
                        "CRS" -> {
                            val data = AviationSerializer.toCrs(record.jsonContent)
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DataRow("CRS/ON", data.crsOn)
                                    DataRow("WO/ON Ref", data.woOnRef)
                                    DataRow("A/C TYPE", data.aircraftType)
                                    DataRow("OWNER", data.owner)
                                    DataRow("MAINTENANCE LOCATION", data.maintenanceLocation)
                                    DataRow("A/C MSN", data.aircraftMsn)
                                    DataRow("ITEM NAME", data.itemName)
                                    DataRow("P/N", data.partNo)
                                    DataRow("S/N", data.serialNo)
                                    DataBlockRow("DETAILED DESCRIPTION CARRIED OUT", data.detailedDescription)
                                    DataRow("INDEPENDENT INSPECTION PERFORMED", data.independentInspection)
                                    DataBlockRow("DEFERRED DEFECTS / ITEMS", data.deferredDefects)
                                }
                            }
                        }
                    }
                }

                // Copy and Actions Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val contentString = getDisplayString(record)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Aviation Document Data", contentString)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 คัดลอกข้อมูลเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("คัดลอกข้อบท")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ปิดหน้าต่าง")
                    }
                }
            }
        }
    }
}

@Composable
fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, fontWeight = FontWeight.Normal, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DataRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
fun DataBlockRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Helper methods
fun getFormTypeName(type: String): String {
    return when (type) {
        "WO" -> "ใบสั่งงาน (Work Order)"
        "WS" -> "ใบสั่งทำ (Work Sheet)"
        "WB" -> "ใบสรุปงาน (Work Brief)"
        "SHR" -> "บันทึกการส่งมอบผลัด (Shift Handover)"
        "CRS" -> "ใบรับรองการปล่อยอากาศยาน (Release to Service)"
        else -> "แบบฟอร์ม"
    }
}

fun formatDateLong(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss น.", java.util.Locale("th", "TH"))
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

fun getDisplayString(record: AviationRecord): String {
    val sb = java.lang.StringBuilder()
    sb.append("=== ${getFormTypeName(record.type)} ===\n")
    sb.append("เลขที่อ้างอิง: ${record.referenceNo}\n")
    sb.append("หัวข้อ/หน่วย: ${record.title}\n")
    sb.append("วันที่ลงระบบ: ${record.date}\n")
    sb.append("วันที่บันทึกประวัติ: ${formatDateLong(record.savedAt)}\n")
    sb.append("\n[รายละเอียดฟอร์ม]\n")

    when (record.type) {
        "WO" -> {
            val d = AviationSerializer.toWo(record.jsonContent)
            sb.append("เลขที่ใบส่งซ่อม: ${d.repairNo}\n")
            sb.append("WO/ON (เลขงาน): ${d.woOn}\n")
            sb.append("วันที่ส่งซ่อม: ${d.repairDate}\n")
            sb.append("ลงวันที่: ${d.docDate}\n")
            sb.append("สายยุทโธปกรณ์: ${d.equipmentLine}\n")
            sb.append("P/N (Part Number): ${d.partNo}\n")
            sb.append("S/N (Serial Number): ${d.serialNo}\n")
            sb.append("ITEM NAME: ${d.itemName}\n")
            sb.append("TSO / TSN: ${d.tsoTsn}\n")
            sb.append("OWNER / หน่วยส่งซ่อม: ${d.owner}\n")
            sb.append("วันที่รับงานซ่อม: ${d.receivedDate}\n")
            sb.append("ตอนรับงานซ่อม: ${d.section}\n")
            sb.append("MAINTENANCE LOCATION: ${d.maintenanceLocation}\n")
            sb.append("ผู้รับงานซ่อม: ${d.receiver}\n")
            sb.append("ลำดับความเร่งด่วน: ${d.priority}\n")
            sb.append("รายละเอียด: ${d.workDetails}\n")
            sb.append("เอกสารอ้างอิง: ${d.refDocs}\n")
        }
        "WS" -> {
            val d = AviationSerializer.toWs(record.jsonContent)
            sb.append("WS/ON: ${d.wsOn}\n")
            sb.append("WO/ON Ref: ${d.woOnRef}\n")
            sb.append("วันที่: ${d.date}\n")
            sb.append("AIRCRAFT TYPE: ${d.aircraftType}\n")
            sb.append("AIRCRAFT MSN: ${d.aircraftMsn}\n")
            sb.append("คู่มือทางเทคนิค: ${d.technicalManual}\n")
        }
        "WB" -> {
            val d = AviationSerializer.toWb(record.jsonContent)
            sb.append("WB/ON: ${d.wbOn}\n")
            sb.append("WS/ON Ref: ${d.wsOnRef}\n")
            sb.append("วันที่: ${d.date}\n")
            sb.append("รายละเอียดผู้ปฏิบัติ: ${d.workTasks}\n")
            sb.append("ผู้สั่งงาน/ผู้ควบคุม: ${d.supervisor}\n")
            sb.append("ผู้ปฏิบัติงาน: ${d.performer}\n")
        }
        "SHR" -> {
            val d = AviationSerializer.toShr(record.jsonContent)
            sb.append("SHR/ON: ${d.shrOn}\n")
            sb.append("WB/ON Ref: ${d.wbOnRef}\n")
            sb.append("วันที่ลงผลัด: ${d.date}\n")
            sb.append("อากาศยาน TYPE: ${d.aircraftType}\n")
            sb.append("MSN: ${d.aircraftMsn}\n")
            sb.append("สถานที่ซ่อมบำรุง: ${d.maintenanceLocation}\n")
            sb.append("WORK SHEET อ้างอิง: ${d.workPackageRef}\n")
            sb.append("วันเวลาเริ่ม: ${d.startDate}\n")
            sb.append("เวลาสิ้นสุดผลัด: ${d.shiftEnd}\n")
            sb.append("เลขงาน WO/NO: ${d.taskWoNo}\n")
            sb.append("ขั้นตอนล่าสุดที่เสร็จ: ${d.lastStepCompleted}\n")
            sb.append("งานค้างส่ง/ข้อควรระวัง: ${d.remainingCaution}\n")
        }
        "CRS" -> {
            val d = AviationSerializer.toCrs(record.jsonContent)
            sb.append("CRS/ON: ${d.crsOn}\n")
            sb.append("WO/ON Ref: ${d.woOnRef}\n")
            sb.append("แบบเครื่อง A/C TYPE: ${d.aircraftType}\n")
            sb.append("หน่วยงานผู้เป็นเจ้าของ: ${d.owner}\n")
            sb.append("สถานที่ปฏิบัติงานซ่อม: ${d.maintenanceLocation}\n")
            sb.append("A/C MSN: ${d.aircraftMsn}\n")
            sb.append("ITEM NAME: ${d.itemName}\n")
            sb.append("P/N: ${d.partNo}\n")
            sb.append("S/N: ${d.serialNo}\n")
            sb.append("รายละเอียดปฏิบัติการซ่อมบำรุง/อะไหล่ที่เปลี่ยน: ${d.detailedDescription}\n")
            sb.append("การตรวจสอบเป็นเอกเทศ: ${d.independentInspection}\n")
            sb.append("ข้อพกพร่องที่ผ่อนผัน: ${d.deferredDefects}\n")
        }
    }
    return sb.toString()
}
