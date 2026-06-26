package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AviationRecord
import com.example.ui.viewmodel.AviationViewModel
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FormType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AviationViewModel,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allSavedRecords.collectAsState()
    
    // Quick statistics
    val countWO = allRecords.count { it.type == "WO" }
    val countWS = allRecords.count { it.type == "WS" }
    val countWB = allRecords.count { it.type == "WB" }
    val countSHR = allRecords.count { it.type == "SHR" }
    val countCRS = allRecords.count { it.type == "CRS" }
    val totalCount = allRecords.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Hero HUD Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ร้อย.ซบร.บ.ทบ.สท.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ระบบจัดการแบบฟอร์มปฏิบัติงานดิจิทัล",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "Aviation Maintenance Management System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }

                    // Display the gorgeous military emblem logo here!
                    AviationUnitLogo(
                        size = 110.dp,
                        showText = false,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Small circular status light
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF14FFEC))
                        .align(Alignment.TopEnd)
                )
            }
        }

        // QR SCANNER INTEGRATIVE CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartScan() }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "สแกน QR แท็กอุปกรณ์ (QR Equipment Scanner)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "สแกนบาร์โค้ด / ป้ายซีเรียลบนชิ้นส่วน เพื่อดึงข้อมูลประวัติงานซ่อมทันที",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stats Dashboard Card Grid
        item {
            Text(
                text = "📊 สถานะการบันทึกข้อมูล (System Stats)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "รวมข้อมูลทั้งหมด",
                    count = totalCount,
                    icon = Icons.Default.Folder,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Work Order (WO)",
                    count = countWO,
                    icon = Icons.Default.Assignment,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Work Sheet (WS)",
                    count = countWS,
                    icon = Icons.Default.Description,
                    color = Color(0xFF00ADB5),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Work Brief (WB)",
                    count = countWB,
                    icon = Icons.Default.RateReview,
                    color = Color(0xFFFF9F43),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Shift Handover (SHR)",
                    count = countSHR,
                    icon = Icons.Default.Sync,
                    color = Color(0xFF6C5CE7),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Release to Service (CRS)",
                    count = countCRS,
                    icon = Icons.Default.FactCheck,
                    color = Color(0xFF10AC84),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Entry shortcuts
        item {
            Text(
                text = "⚡ ทางลัดสร้างเอกสาร (Quick Entry)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShortcutRow(
                    label = "1. Work Order - ใบสั่งงาน",
                    icon = Icons.Default.Assignment,
                    onClick = { viewModel.setFormType(FormType.WO) }
                )
                ShortcutRow(
                    label = "2. Work Sheet - ใบสั่งทำ",
                    icon = Icons.Default.Description,
                    onClick = { viewModel.setFormType(FormType.WS) }
                )
                ShortcutRow(
                    label = "3. Work Brief - ใบสรุปงาน",
                    icon = Icons.Default.RateReview,
                    onClick = { viewModel.setFormType(FormType.WB) }
                )
                ShortcutRow(
                    label = "4. Shift Handover - บันทึกผลัด",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.setFormType(FormType.SHR) }
                )
                ShortcutRow(
                    label = "5. Release Certificate (CRS)",
                    icon = Icons.Default.FactCheck,
                    onClick = { viewModel.setFormType(FormType.CRS) }
                )
            }
        }

        // Recent Entries list header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏳ รายการล่าสุด (Recent Updates)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (allRecords.isNotEmpty()) {
                    TextButton(onClick = { viewModel.setScreen(AppScreen.HISTORY) }) {
                        Text("ดูประวัติทั้งหมด", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent records empty state or cards
        if (allRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ยังไม่มีประวัติการบันทึกข้อมูลแบบฟอร์ม\nเลือกเมนูด้านล่างหรือทางลัดเพื่อเริ่มกรอกข้อมูล",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allRecords.take(3)) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectRecordToView(record)
                            viewModel.setScreen(AppScreen.HISTORY)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Badge denoting record type
                        Box(
                            modifier = Modifier
                                .size(44.dp)
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
                                fontSize = 14.sp
                            )
                        }

                        // Text content
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "เลขที่: ${record.referenceNo}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                StatusBadgeMini(status = record.status)
                            }
                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Timestamp / edit date
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = record.date.ifBlank { "ไม่ระบุวัน" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formatTime(record.savedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ShortcutRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm น.", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun StatusBadgeMini(status: String, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) = when (status) {
        "Completed" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "เสร็จสิ้น")
        "In-Progress" -> Triple(Color(0xFFFFF9C4), Color(0xFFF57F17), "กำลังทำ")
        else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "รอดำเนินการ")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
