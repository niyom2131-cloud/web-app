package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FormScreens
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AviationViewModel
import com.example.ui.viewmodel.FormType
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: AviationViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val currentScreen by viewModel.currentScreen.collectAsState()
                val selectedFormType by viewModel.selectedFormType.collectAsState()
                
                // Observe feedback messages from ViewModel securely
                LaunchedEffect(Unit) {
                    viewModel.saveStatusMessage.collectLatest { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Handyman,
                                        contentDescription = "ตอนไฟฟ้า",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "ตอนไฟฟ้า",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "AVIATION MAINTENANCE SYSTEM",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            
                            // User icon/Quick Actions menu
                            var expandedMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { expandedMenu = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                        text = { Text("📋 สถิติภาพรวม") },
                                        onClick = {
                                            viewModel.setScreen(AppScreen.DASHBOARD)
                                            expandedMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                        text = { Text("🔎 ประวัติข้อมูลฟอร์ม") },
                                        onClick = {
                                            viewModel.setScreen(AppScreen.HISTORY)
                                            expandedMenu = false
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        text = { Text("⚠️ เคลียร์ประวัติทั้งหมด", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            viewModel.clearAllRecords()
                                            expandedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DASHBOARD,
                                onClick = { viewModel.setScreen(AppScreen.DASHBOARD) },
                                label = { Text("หน้าแรก", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.DASHBOARD) Icons.Default.Dashboard else Icons.Default.Dashboard,
                                        contentDescription = "Dashboard"
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.FORMS,
                                onClick = { viewModel.setScreen(AppScreen.FORMS) },
                                label = { Text("กรอกข้อมูล", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.FORMS) Icons.Default.EditNote else Icons.Default.EditNote,
                                        contentDescription = "Forms"
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HISTORY,
                                onClick = { viewModel.setScreen(AppScreen.HISTORY) },
                                label = { Text("ประวัติ", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.HISTORY) Icons.Default.History else Icons.Default.History,
                                        contentDescription = "History"
                                    )
                                }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                (fadeIn() + slideInHorizontally { width -> width / 4 } togetherWith
                                        fadeOut() + slideOutHorizontally { width -> -width / 4 })
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.FORMS -> Column {
                                    // Row of fast quick selectors for the active form
                                    ScrollableFormTabs(
                                        selected = selectedFormType,
                                        onSelect = { form -> viewModel.setFormType(form) }
                                    )
                                    FormScreens(
                                        viewModel = viewModel,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                AppScreen.HISTORY -> HistoryScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollableFormTabs(
    selected: FormType,
    onSelect: (FormType) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormType.values().forEach { formType ->
            val isSelected = selected == formType
            val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val chipContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            
            Surface(
                onClick = { onSelect(formType) },
                shape = CircleShape,
                color = chipBg,
                contentColor = chipContentColor,
                tonalElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (formType) {
                        FormType.WO -> Icons.Default.Description
                        FormType.WS -> Icons.Default.Assignment
                        FormType.WB -> Icons.Default.EditNote
                        FormType.SHR -> Icons.Default.Sync
                        FormType.CRS -> Icons.Default.VerifiedUser
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (formType) {
                            FormType.WO -> "WO"
                            FormType.WS -> "WS"
                            FormType.WB -> "WB"
                            FormType.SHR -> "SHR"
                            FormType.CRS -> "CRS"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

