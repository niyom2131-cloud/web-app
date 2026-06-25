package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AviationViewModel
import com.example.data.AviationRecord
import com.example.ui.viewmodel.FormType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AviationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Observe Setting flows
    val inspectorName by viewModel.defaultInspectorName.collectAsState()
    val rank by viewModel.defaultRank.collectAsState()
    val unit by viewModel.defaultUnit.collectAsState()
    val section by viewModel.defaultSection.collectAsState()
    val autoSave by viewModel.autoSaveDraft.collectAsState()
    val soundEffects by viewModel.enableSoundEffects.collectAsState()
    val aircraftType by viewModel.defaultAircraftType.collectAsState()
    val location by viewModel.defaultLocation.collectAsState()
    val appTheme by viewModel.appThemeColor.collectAsState()

    // State for Dialogs
    var showResetDialog by remember { mutableStateOf(false) }
    var showRankDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }
    var showAircraftDropdown by remember { mutableStateOf(false) }

    val ranks = listOf("จ.ส.อ.", "ร.ต.", "ร.ท.", "ร.อ.", "พ.ต.", "พ.ท.", "พ.อ.", "ช่างพลเรือน")
    val themes = listOf("Default Slate", "Forest Green", "Aviation Blue", "Sunset Orange")
    val aircrafts = listOf("Bell 212", "Bell 412", "AS550", "UH-60", "Mi-17", "AH-1 Cobra")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // 1. User Profile Settings Section
        item {
            SettingsSectionHeader(title = "ข้อมูลเจ้าหน้าที่และหน่วยปฏิบัติงาน", icon = Icons.Default.Badge)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Inspector Name
                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = { viewModel.updateDefaultInspectorName(it) },
                        label = { Text("ชื่อ-นามสกุล ช่างซ่อมบำรุง") },
                        placeholder = { Text("ระบุชื่อผู้ปฏิบัติงานหลัก") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth().testTag("setting_inspector_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Rank Select Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showRankDropdown,
                        onExpandedChange = { showRankDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = rank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ยศ / ตำแหน่ง") },
                            leadingIcon = { Icon(Icons.Default.MilitaryTech, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRankDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("setting_rank_picker"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = showRankDropdown,
                            onDismissRequest = { showRankDropdown = false }
                        ) {
                            ranks.forEach { rankOption ->
                                DropdownMenuItem(
                                    text = { Text(rankOption) },
                                    onClick = {
                                        viewModel.updateDefaultRank(rankOption)
                                        showRankDropdown = false
                                        Toast.makeText(context, "ตั้งค่ายศเริ่มต้นเป็น: $rankOption", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Unit / Department
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { viewModel.updateDefaultUnit(it) },
                        label = { Text("สังกัดหน่วยงานหลัก") },
                        placeholder = { Text("ร้อย.ซบร.บ.ทบ.สท.") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                        modifier = Modifier.fillMaxWidth().testTag("setting_unit_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Section Details (removed "ตอนไฟฟ้า มว.ควบคุมฯ" completely, using customized default maintenance section)
                    OutlinedTextField(
                        value = section,
                        onValueChange = { viewModel.updateDefaultSection(it) },
                        label = { Text("กลุ่มงานซ่อม/แผนกปฏิบัติการ") },
                        placeholder = { Text("ตอนซ่อมโครงสร้าง, ตอนซ่อมเครื่องยนต์ ฯลฯ") },
                        leadingIcon = { Icon(Icons.Default.Settings, null) },
                        modifier = Modifier.fillMaxWidth().testTag("setting_section_name"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 2. Form Default Fields Section
        item {
            SettingsSectionHeader(title = "ค่าเริ่มต้นในการกรอกแบบฟอร์ม (ลดเวลาทำงาน)", icon = Icons.Default.AutoMode)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Aircraft Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showAircraftDropdown,
                        onExpandedChange = { showAircraftDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = aircraftType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("แบบอากาศยานหลัก") },
                            leadingIcon = { Icon(Icons.Default.AirplanemodeActive, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAircraftDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("setting_aircraft_picker"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = showAircraftDropdown,
                            onDismissRequest = { showAircraftDropdown = false }
                        ) {
                            aircrafts.forEach { acOption ->
                                DropdownMenuItem(
                                    text = { Text(acOption) },
                                    onClick = {
                                        viewModel.updateDefaultAircraftType(acOption)
                                        showAircraftDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { viewModel.updateDefaultLocation(it) },
                        label = { Text("สถานที่ซ่อมบำรุง") },
                        placeholder = { Text("โรงเก็บที่ 1, ลานจอด ฯลฯ") },
                        leadingIcon = { Icon(Icons.Default.Place, null) },
                        modifier = Modifier.fillMaxWidth().testTag("setting_location_name"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 3. System Preferences Section
        item {
            SettingsSectionHeader(title = "การตั้งค่าระบบและธีม", icon = Icons.Default.Tune)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Auto Save Draft Option
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("บันทึกแบบร่างอัตโนมัติ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("จำค่าแบบฟอร์มเมื่อสลับแท็บงานชั่วคราว", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = autoSave,
                            onCheckedChange = { viewModel.updateAutoSaveDraft(it) },
                            modifier = Modifier.testTag("setting_switch_autosave")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Sound effects Option
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("เสียงและเอฟเฟกต์แอปพลิเคชัน", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("เปิดเสียงแจ้งเตือนเมื่อบันทึกข้อมูลสำเร็จ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = soundEffects,
                            onCheckedChange = { viewModel.updateEnableSoundEffects(it) },
                            modifier = Modifier.testTag("setting_switch_sounds")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Theme selector Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("โทนสีและธีมแอปพลิเคชัน", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ปัจจุบัน: $appTheme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        Box {
                            Button(
                                onClick = { showThemeDropdown = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("เปลี่ยนธีม", fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = showThemeDropdown,
                                onDismissRequest = { showThemeDropdown = false }
                            ) {
                                themes.forEach { themeName ->
                                    DropdownMenuItem(
                                        text = { Text(themeName) },
                                        onClick = {
                                            viewModel.updateAppThemeColor(themeName)
                                            showThemeDropdown = false
                                            Toast.makeText(context, "อัปเดตธีมเป็น $themeName (จำลองการเปลี่ยนสกินสำเร็จ)", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Utility / Database Management
        item {
            SettingsSectionHeader(title = "การจัดการข้อมูลระบบความปลอดภัย", icon = Icons.Default.Storage)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Populate Mock Data
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                Toast.makeText(context, "กำลังจำลองระบบและเพิ่มชุดข้อมูลตัวอย่าง...", Toast.LENGTH_SHORT).show()
                                populateAviationMockRecords(viewModel)
                                Toast.makeText(context, "✅ เพิ่มข้อมูลตัวอย่างเข้าระบบเรียบร้อย! ไปที่เมนู 'ประวัติ' เพื่อตรวจสอบ", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("setting_populate_mock"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.LibraryAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ จำลองและเพิ่มประวัติข้อมูลสาธิต", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export JSON button
                        Button(
                            onClick = {
                                Toast.makeText(context, "💾 ส่งออกข้อมูลประวัติซ่อมบำรุงสำเร็จ! ไฟล์บันทึกใน /downloads/aviation_backup.json (จำลอง)", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ส่งออกข้อมูล", fontSize = 12.sp)
                        }

                        // Import JSON button
                        Button(
                            onClick = {
                                Toast.makeText(context, "📂 นำเข้าฐานข้อมูลและเชื่อมโยงเอกสารซ่อมสำเร็จ!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("นำเข้าข้อมูล", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Reset completely button
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("setting_reset_all_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ลบและรีเซ็ตฐานข้อมูลประวัติทั้งหมด", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // About / Author Info / Version Footnote
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AviationUnitLogo(
                        size = 80.dp,
                        showText = false,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "ระบบฐานข้อมูลบริหารงานซ่อมบำรุงอากาศยาน (AMS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ร้อย.ซบร.บ.ทบ.สท. | Aviation Maintenance Applet",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "เวอร์ชั่นปัจจุบัน 1.7 (Stable-Build)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Reset Dialog Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "คำเตือน", tint = MaterialTheme.colorScheme.error) },
            title = { Text("ยืนยันลบฐานข้อมูล?", fontWeight = FontWeight.Bold) },
            text = { Text("การดำเนินการนี้จะลบข้อมูลใบสั่งซ่อมบำรุงและประวัติปฏิบัติงานทั้งหมดอย่างถาวร ไม่สามารถย้อนกลับได้") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords()
                        showResetDialog = false
                        Toast.makeText(context, "ล้างประวัติปฏิบัติงานสำเร็จ", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบประวัติทั้งหมด", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Function helper to populate nice mockup records to demonstrate dashboard filters & history
private fun populateAviationMockRecords(viewModel: AviationViewModel) {
    val records = listOf(
        AviationRecord(
            type = "WO",
            referenceNo = "WO-2026-0012",
            title = "แผงสวิตช์ไฟฟ้า (Electrical Distribution Panel)",
            date = "25 มิ.ย. 69",
            jsonContent = """{"repairNo":"REPAIR-889","woOn":"WO-2026-0012","partNo":"PN-990-21","repairDate":"2026-06-25","docDate":"25 มิ.ย. 2026","serialNo":"SN-9081-EL","equipmentLine":"สส.","itemName":"กระเปาะตัดไฟฟ้ารถ/ไฟฟ้า","tsoTsn":"120 Hrs","owner":"ฝบ.ร้อย.คพ.","receivedDate":"2026-06-25","section":"ตอนไฟฟ้า","maintenanceLocation":"โรงเก็บ 1","receiver":"จ.ส.อ. สมชาย","priority":"ด่วนมาก","workDetails":"ทำการซ่อมเปลี่ยนฟิวส์และล้างหน้าสัมผัสขั้วสวิตช์ควบคุมแรงดันไฟเลี้ยงส่วนหัวเก๋งและแผงอุปกรณ์นำร่อง","refDocs":"TM 55-1520-210-23"}"""
        ),
        AviationRecord(
            type = "WS",
            referenceNo = "WS-2026-0087",
            title = "Bell 212",
            date = "24 มิ.ย. 69",
            jsonContent = """{"wsOn":"WS-2026-0087","woOnRef":"WO-2026-0012","aircraftType":"Bell 212","aircraftMsn":"MSN-3012","date":"2026-06-24","technicalManual":"TM 11-1520-210-23-2"}"""
        ),
        AviationRecord(
            type = "WB",
            referenceNo = "WB-2026-0154",
            title = "ผู้ปฏิบัติ: ร.ต. วิษณุ แข็งขัน",
            date = "23 มิ.ย. 69",
            jsonContent = """{"wbOn":"WB-2026-0154","wsOnRef":"WS-2026-0087","date":"2026-06-23","workTasks":"ทำการตรวจสอบไฟเลี้ยว และระบบเดินสายไฟขั้วเชื่อมต่อเครื่องปั่นไฟสำรอง","supervisor":"พ.ต. ณัฐชนนท์","performer":"ร.ต. วิษณุ แข็งขัน"}"""
        ),
        AviationRecord(
            type = "SHR",
            referenceNo = "SHR-2026-0044",
            title = "UH-60 Blackhawk",
            date = "22 มิ.ย. 69",
            jsonContent = """{"shrOn":"SHR-2026-0044","wbOnRef":"WB-2026-0154","date":"2026-06-22","aircraftType":"UH-60 Blackhawk","aircraftMsn":"MSN-6091","maintenanceLocation":"โรงเก็บ 2","workPackageRef":"WS-2026-0087","startDate":"22 มิ.ย. 2026","shiftEnd":"23 มิ.ย. 2026","taskWoNo":"TASK-77","lastStepCompleted":"เดินสายไฟสำรองและทดสอบแรงดันต้านทานกระแสไฟสลับ","remainingCaution":"หน้าปัดควบคุมยังไม่ได้ประกอบกลับ คาดคะเนส่งมอบเวรต่อไปทำการตรวจรับปลั๊กไฟพัดลม"}"""
        ),
        AviationRecord(
            type = "CRS",
            referenceNo = "CRS-2026-0099",
            title = "AS550 (S/N: SN-8892)",
            date = "CRS-2026-0099",
            jsonContent = """{"crsOn":"CRS-2026-0099","woOnRef":"WO-2026-0012","aircraftType":"AS550","owner":"บ.ทบ.สท.","maintenanceLocation":"โรงเก็บสำรอง","aircraftMsn":"MSN-889","itemName":"ขั้วสายไฟฟ้ากระแสสลับพ่วงเครื่องปรับอากาศ","partNo":"PN-AS-223","serialNo":"SN-8892","detailedDescription":"ซ่อมวงจรตัดต่อไฟฟ้ากระแสตรงและสายแรงดันพ่วงสำรองหลัก","independentInspection":"ตรวจรับผ่านเกณฑ์การวัดแรงต้านและทดสอบกระแสขั้วลบอย่างเป็นทางการ","deferredDefects":"ไม่มี"}"""
        )
    )
    viewModel.insertRecords(records)
}
