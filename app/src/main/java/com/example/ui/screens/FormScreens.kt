package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AviationViewModel
import com.example.ui.viewmodel.FormType
import java.util.*

@Composable
fun FormScreens(
    viewModel: AviationViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFormType by viewModel.selectedFormType.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        // Form Title / Subtitle Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AviationUnitLogoMini(size = 44.dp)
                    Column {
                        Text(
                            text = getFormTitle(selectedFormType),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "ร้อย.ซบร.บ.ทบ.สท. (Aviation Maintenance Form)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Active Form Layout
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedFormType) {
                        FormType.WO -> WorkOrderForm(viewModel)
                        FormType.WS -> WorkSheetForm(viewModel)
                        FormType.WB -> WorkBriefForm(viewModel)
                        FormType.SHR -> ShiftHandoverForm(viewModel)
                        FormType.CRS -> CertificateReleaseForm(viewModel)
                    }
                }
            }
        }

        // Initial Status Selector Card
        item {
            val activeFormStatus by viewModel.activeFormStatus.collectAsState()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚙️ กำหนดสถานะงานซ่อม (Repair Task Status)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "เลือกสถานะการซ่อมบำรุงขั้นแรกสำหรับใบงานนี้",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusOptions = listOf(
                            Pair("Pending", "รอดำเนินการ"),
                            Pair("In-Progress", "กำลังทำ"),
                            Pair("Completed", "เสร็จสิ้น")
                        )
                        statusOptions.forEach { option ->
                            val statusKey = option.first
                            val statusLabel = option.second
                            val isSelected = activeFormStatus == statusKey
                            val chipBg = when (statusKey) {
                                "Completed" -> if (isSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                                "In-Progress" -> if (isSelected) Color(0xFFF57F17) else Color(0xFFFFF9C4)
                                else -> if (isSelected) Color(0xFFC62828) else Color(0xFFFFEBEE)
                            }
                            val chipContentColor = when (statusKey) {
                                "Completed" -> if (isSelected) Color.White else Color(0xFF2E7D32)
                                "In-Progress" -> if (isSelected) Color.White else Color(0xFFF57F17)
                                else -> if (isSelected) Color.White else Color(0xFFC62828)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .clickable { viewModel.updateActiveFormStatus(statusKey) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = chipContentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Standard Submit Button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            val context = LocalContext.current
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveActiveForm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "บันทึกข้อมูล ${getFormAbbr(selectedFormType)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                FilledTonalButton(
                    onClick = {
                        val record = viewModel.getRecordFromCurrentDraft()
                        if (record != null) {
                            val file = com.example.utils.PdfExporter.exportSingleRecordToPdf(context, record)
                            if (file != null) {
                                com.example.utils.PdfExporter.sharePdfFile(context, file)
                            } else {
                                android.widget.Toast.makeText(context, "เกิดข้อผิดพลาดในการสร้าง PDF", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "⚠️ โปรดระบุรหัสเอกสารหลักบังคับ (*) ก่อนส่งออก PDF",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "พิมพ์ / ส่งออกรายงาน PDF (Draft)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ---------------- 1. WORK ORDER FORM ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderForm(viewModel: AviationViewModel) {
    val data by viewModel.woDraft.collectAsState()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "ข้อมูลเอกสาร")
        
        OutlinedTextField(
            value = data.repairNo,
            onValueChange = { value -> viewModel.updateWo { it.copy(repairNo = value) } },
            label = { Text("เลขที่ใบส่งซ่อม") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Pin, null) }
        )

        OutlinedTextField(
            value = data.woOn,
            onValueChange = { value -> viewModel.updateWo { it.copy(woOn = value) } },
            label = { Text("WO/ON (เลขงาน) *") },
            isError = data.woOn.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Badge, null) }
        )

        DatePickerField(
            label = "วันที่ส่งซ่อม",
            value = data.repairDate,
            onDateSelected = { value -> viewModel.updateWo { it.copy(repairDate = value) } }
        )

        DatePickerField(
            label = "ลงวันที่",
            value = data.docDate,
            onDateSelected = { value -> viewModel.updateWo { it.copy(docDate = value) } }
        )

        SectionTitle(title = "ข้อมูลอุปกรณ์ / อะไหล่")

        OutlinedTextField(
            value = data.partNo,
            onValueChange = { value -> viewModel.updateWo { it.copy(partNo = value) } },
            label = { Text("P/N (Part Number)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Settings, null) }
        )

        OutlinedTextField(
            value = data.serialNo,
            onValueChange = { value -> viewModel.updateWo { it.copy(serialNo = value) } },
            label = { Text("S/N (Serial Number)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.QrCode, null) }
        )

        OutlinedTextField(
            value = data.itemName,
            onValueChange = { value -> viewModel.updateWo { it.copy(itemName = value) } },
            label = { Text("ITEM NAME") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Info, null) }
        )

        OutlinedTextField(
            value = data.tsoTsn,
            onValueChange = { value -> viewModel.updateWo { it.copy(tsoTsn = value) } },
            label = { Text("TSO / TSN") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Timelapse, null) }
        )

        DropdownSelector(
            label = "สายยุทโธปกรณ์",
            options = listOf("สพ.", "สส.", "ขส.", "ช.", "พบ."),
            selectedOption = data.equipmentLine,
            onOptionSelected = { value -> viewModel.updateWo { it.copy(equipmentLine = value) } }
        )

        SectionTitle(title = "ข้อมูลหน่วยและสถานะ")

        OutlinedTextField(
            value = data.owner,
            onValueChange = { value -> viewModel.updateWo { it.copy(owner = value) } },
            label = { Text("OWNER / หน่วยส่งซ่อม") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, null) }
        )

        DatePickerField(
            label = "วันที่รับงานซ่อม",
            value = data.receivedDate,
            onDateSelected = { value -> viewModel.updateWo { it.copy(receivedDate = value) } }
        )

        DropdownSelector(
            label = "ตอนรับงานซ่อม",
            options = listOf("ตอนไฟฟ้า", "ตอนซ่อมโครงสร้าง", "ตอนถ่ายทอดกำลัง", "ตอนเย็บหนัง", "ตอนบริภัณฑ์ภาคพื้น", "อื่นๆ"),
            selectedOption = data.section,
            onOptionSelected = { value -> viewModel.updateWo { it.copy(section = value) } }
        )

        OutlinedTextField(
            value = data.maintenanceLocation,
            onValueChange = { value -> viewModel.updateWo { it.copy(maintenanceLocation = value) } },
            label = { Text("MAINTENANCE LOCATION") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Place, null) }
        )

        OutlinedTextField(
            value = data.receiver,
            onValueChange = { value -> viewModel.updateWo { it.copy(receiver = value) } },
            label = { Text("ผู้รับงานซ่อม") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )

        DropdownSelector(
            label = "ลำดับความเร่งด่วน",
            options = listOf("ปกติ", "ด่วน", "ด่วนมาก", "ด่วนที่สุด"),
            selectedOption = data.priority,
            onOptionSelected = { value -> viewModel.updateWo { it.copy(priority = value) } }
        )

        SectionTitle(title = "รายละเอียดกุมงาน")

        OutlinedTextField(
            value = data.workDetails,
            onValueChange = { value -> viewModel.updateWo { it.copy(workDetails = value) } },
            label = { Text("WORK DETAILS") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )

        OutlinedTextField(
            value = data.refDocs,
            onValueChange = { value -> viewModel.updateWo { it.copy(refDocs = value) } },
            label = { Text("REFERENCE DOCUMENTS") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Book, null) }
        )
    }
}

// ---------------- 2. WORK SHEET FORM ----------------
@Composable
fun WorkSheetForm(viewModel: AviationViewModel) {
    val data by viewModel.wsDraft.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "ข้อมูลจำเพาะเอกสาร WS")

        OutlinedTextField(
            value = data.wsOn,
            onValueChange = { value -> viewModel.updateWs { it.copy(wsOn = value) } },
            label = { Text("WS/ON *") },
            isError = data.wsOn.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Pin, null) }
        )

        OutlinedTextField(
            value = data.woOnRef,
            onValueChange = { value -> viewModel.updateWs { it.copy(woOnRef = value) } },
            label = { Text("WO/ON") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Assignment, null) }
        )

        DatePickerField(
            label = "DATE",
            value = data.date,
            onDateSelected = { value -> viewModel.updateWs { it.copy(date = value) } }
        )

        SectionTitle(title = "ข้อมูลอากาศยาน")

        OutlinedTextField(
            value = data.aircraftType,
            onValueChange = { value -> viewModel.updateWs { it.copy(aircraftType = value) } },
            label = { Text("AIRCRAFT TYPE") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Flight, null) }
        )

        OutlinedTextField(
            value = data.aircraftMsn,
            onValueChange = { value -> viewModel.updateWs { it.copy(aircraftMsn = value) } },
            label = { Text("AIRCRAFT MSN") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) }
        )

        SectionTitle(title = "คู่มือและรายละเอียดการรับมอบ")

        OutlinedTextField(
            value = data.technicalManual,
            onValueChange = { value -> viewModel.updateWs { it.copy(technicalManual = value) } },
            label = { Text("OPERATION MAINTENANCE TECHNICAL MANUAL") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8
        )
    }
}

// ---------------- 3. WORK BRIEF FORM ----------------
@Composable
fun WorkBriefForm(viewModel: AviationViewModel) {
    val data by viewModel.wbDraft.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "ข้อมูลใบสรุปงาน WB")

        OutlinedTextField(
            value = data.wbOn,
            onValueChange = { value -> viewModel.updateWb { it.copy(wbOn = value) } },
            label = { Text("WB/ON *") },
            isError = data.wbOn.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Pin, null) }
        )

        OutlinedTextField(
            value = data.wsOnRef,
            onValueChange = { value -> viewModel.updateWb { it.copy(wsOnRef = value) } },
            label = { Text("WS/ON") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Description, null) }
        )

        DatePickerField(
            label = "DATE",
            value = data.date,
            onDateSelected = { value -> viewModel.updateWb { it.copy(date = value) } }
        )

        SectionTitle(title = "การมอบหมายและรายละเอียดงาน")

        OutlinedTextField(
            value = data.workTasks,
            onValueChange = { value -> viewModel.updateWb { it.copy(workTasks = value) } },
            label = { Text("WORK TASKS OPERATION") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8
        )

        OutlinedTextField(
            value = data.supervisor,
            onValueChange = { value -> viewModel.updateWb { it.copy(supervisor = value) } },
            label = { Text("SUPERVISER / CONTROLLER") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Shield, null) }
        )

        OutlinedTextField(
            value = data.performer,
            onValueChange = { value -> viewModel.updateWb { it.copy(performer = value) } },
            label = { Text("PERFORMER") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Engineering, null) }
        )
    }
}

// ---------------- 4. SHIFT HANDOVER FORM ----------------
@Composable
fun ShiftHandoverForm(viewModel: AviationViewModel) {
    val data by viewModel.shrDraft.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "ข้อมูลเอกสารส่งมอบเวร")

        OutlinedTextField(
            value = data.shrOn,
            onValueChange = { value -> viewModel.updateShr { it.copy(shrOn = value) } },
            label = { Text("SHR/ON *") },
            isError = data.shrOn.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Pin, null) }
        )

        OutlinedTextField(
            value = data.wbOnRef,
            onValueChange = { value -> viewModel.updateShr { it.copy(wbOnRef = value) } },
            label = { Text("WB/ON") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.RateReview, null) }
        )

        DatePickerField(
            label = "DATE",
            value = data.date,
            onDateSelected = { value -> viewModel.updateShr { it.copy(date = value) } }
        )

        SectionTitle(title = "1. TASK REFERENCE")

        OutlinedTextField(
            value = data.aircraftType,
            onValueChange = { value -> viewModel.updateShr { it.copy(aircraftType = value) } },
            label = { Text("AIRCRAFT TYPE") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Flight, null) }
        )

        OutlinedTextField(
            value = data.aircraftMsn,
            onValueChange = { value -> viewModel.updateShr { it.copy(aircraftMsn = value) } },
            label = { Text("AIRCRAFT MSN") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) }
        )

        OutlinedTextField(
            value = data.maintenanceLocation,
            onValueChange = { value -> viewModel.updateShr { it.copy(maintenanceLocation = value) } },
            label = { Text("MAINTENANCE LOCATION") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Place, null) }
        )

        OutlinedTextField(
            value = data.workPackageRef,
            onValueChange = { value -> viewModel.updateShr { it.copy(workPackageRef = value) } },
            label = { Text("WORK PACKAGE REF: WORK SHEET") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.DocumentScanner, null) }
        )

        DatePickerField(
            label = "START DATE",
            value = data.startDate,
            onDateSelected = { value -> viewModel.updateShr { it.copy(startDate = value) } }
        )

        DatePickerField(
            label = "SHIFT END",
            value = data.shiftEnd,
            onDateSelected = { value -> viewModel.updateShr { it.copy(shiftEnd = value) } }
        )

        SectionTitle(title = "2. WORK IN NON PROGRESS STATUS")

        OutlinedTextField(
            value = data.taskWoNo,
            onValueChange = { value -> viewModel.updateShr { it.copy(taskWoNo = value) } },
            label = { Text("TASK WO/NO") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Assignment, null) }
        )

        OutlinedTextField(
            value = data.lastStepCompleted,
            onValueChange = { value -> viewModel.updateShr { it.copy(lastStepCompleted = value) } },
            label = { Text("LAST STEP COMPLETED") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.DoneAll, null) }
        )

        OutlinedTextField(
            value = data.remainingCaution,
            onValueChange = { value -> viewModel.updateShr { it.copy(remainingCaution = value) } },
            label = { Text("REMAINING / CAUTION") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}

// ---------------- 5. CERTIFICATE OF RELEASE FORM ----------------
@Composable
fun CertificateReleaseForm(viewModel: AviationViewModel) {
    val data by viewModel.crsDraft.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "ข้อมูลปล่อยตัวเครื่องพับ/เครื่องบิน (CRS)")

        OutlinedTextField(
            value = data.crsOn,
            onValueChange = { value -> viewModel.updateCrs { it.copy(crsOn = value) } },
            label = { Text("CRS/ON *") },
            isError = data.crsOn.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Pin, null) }
        )

        OutlinedTextField(
            value = data.woOnRef,
            onValueChange = { value -> viewModel.updateCrs { it.copy(woOnRef = value) } },
            label = { Text("WO/ON") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Assignment, null) }
        )

        OutlinedTextField(
            value = data.aircraftType,
            onValueChange = { value -> viewModel.updateCrs { it.copy(aircraftType = value) } },
            label = { Text("A/C TYPE") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Flight, null) }
        )

        OutlinedTextField(
            value = data.owner,
            onValueChange = { value -> viewModel.updateCrs { it.copy(owner = value) } },
            label = { Text("OWNER") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, null) }
        )

        OutlinedTextField(
            value = data.maintenanceLocation,
            onValueChange = { value -> viewModel.updateCrs { it.copy(maintenanceLocation = value) } },
            label = { Text("MAINTENANCE LOCATION") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Place, null) }
        )

        OutlinedTextField(
            value = data.aircraftMsn,
            onValueChange = { value -> viewModel.updateCrs { it.copy(aircraftMsn = value) } },
            label = { Text("A/C MSN") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) }
        )

        OutlinedTextField(
            value = data.itemName,
            onValueChange = { value -> viewModel.updateCrs { it.copy(itemName = value) } },
            label = { Text("ITEM NAME") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Info, null) }
        )

        OutlinedTextField(
            value = data.partNo,
            onValueChange = { value -> viewModel.updateCrs { it.copy(partNo = value) } },
            label = { Text("P/N (Part Number)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Settings, null) }
        )

        OutlinedTextField(
            value = data.serialNo,
            onValueChange = { value -> viewModel.updateCrs { it.copy(serialNo = value) } },
            label = { Text("S/N (Serial Number)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.QrCode, null) }
        )

        SectionTitle(title = "รายละเอียดการซ่อมบำรุงและข้อบกพร่อง")

        OutlinedTextField(
            value = data.detailedDescription,
            onValueChange = { value -> viewModel.updateCrs { it.copy(detailedDescription = value) } },
            label = { Text("DETAILED DESCRIPTION... REPLACED PARTS, REPAIRS...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8
        )

        OutlinedTextField(
            value = data.independentInspection,
            onValueChange = { value -> viewModel.updateCrs { it.copy(independentInspection = value) } },
            label = { Text("INDEPENDENT INSPECTION PERFORMED (IF THERE)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.VerifiedUser, null) }
        )

        OutlinedTextField(
            value = data.deferredDefects,
            onValueChange = { value -> viewModel.updateCrs { it.copy(deferredDefects = value) } },
            label = { Text("DEFERRED DEFECTS / ITEMS") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}

// ---------------- UI UTILS & HELPERS ----------------
@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Format to YYYY-MM-DD
            val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(dateStr)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        enabled = false, // Disables manual key input, clicks handled by modifier
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        leadingIcon = { Icon(Icons.Default.DateRange, null) },
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Choose Date")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption.ifBlank { "เลือก${label}" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            leadingIcon = { Icon(Icons.Default.List, null) },
            colors = OutlinedTextFieldDefaults.colors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        onOptionSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getFormTitle(type: FormType): String {
    return when (type) {
        FormType.WO -> "📋 ใบสั่งงาน (Work Order - WO)"
        FormType.WS -> "📄 ใบสั่งทำ (Work Sheet - WS)"
        FormType.WB -> "📝 สรุปงาน (Work Brief - WB)"
        FormType.SHR -> "🔄 บันทึกการส่งมอบผลัด (SHR)"
        FormType.CRS -> "✅ ใบรับรองการปล่อยอากาศยาน (CRS)"
    }
}

fun getFormAbbr(type: FormType): String {
    return when (type) {
        FormType.WO -> "WO"
        FormType.WS -> "WS"
        FormType.WB -> "WB"
        FormType.SHR -> "SHR"
        FormType.CRS -> "CRS"
    }
}
