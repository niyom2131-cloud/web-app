package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (cameraPermissionState.status.isGranted) {
                    CameraScannerView(
                        onCodeScanned = onCodeScanned,
                        onDismiss = onDismiss
                    )
                } else {
                    PermissionRequestView(
                        permissionState = cameraPermissionState,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScannerView(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasScanned by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var manualInputText by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Sound effect setup
    val playBeep = {
        try {
            val mp = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            mp?.start()
            mp?.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Camera PreviewView reference
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Set up ML Kit scanner
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && !hasScanned) {
                                            hasScanned = true
                                            playBeep()
                                            onCodeScanned(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    // Handle failure if needed
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera live stream preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay with a clear focal cutout box in center
        ScannerOverlay()

        // Scan controls (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Text(
                text = "เครื่องสแกนป้ายอุปกรณ์",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = {
                    torchEnabled = !torchEnabled
                    cameraControl?.enableTorch(torchEnabled)
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (torchEnabled) Color.Yellow else Color.White
                )
            }
        }

        // Search options & instructions (Bottom overlay)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.75f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "วาง QR Code / บาร์โค้ดให้อยู่ในกรอบ",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ระบบจะค้นหาประวัติงานซ่อมบำรุงที่ตรงกับซีเรียลหรือประเภทอุปกรณ์ทันที",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (showManualInput) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            placeholder = { Text("ระบุรหัสอุปกรณ์เอง...", fontSize = 13.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (manualInputText.isNotBlank()) {
                                    playBeep()
                                    onCodeScanned(manualInputText)
                                }
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (manualInputText.isNotBlank()) {
                                    playBeep()
                                    onCodeScanned(manualInputText)
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ค้นหา", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { showManualInput = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("กรอกรหัสอุปกรณ์ด้วยตนเอง", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserAnimation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Define a 260dp square box in the center
        val boxSize = 260.dp.toPx()
        val left = (width - boxSize) / 2f
        val top = (height - boxSize) / 2f
        val right = left + boxSize
        val bottom = top + boxSize

        val overlayColor = Color.Black.copy(alpha = 0.65f)

        // 1. Top overlay rectangle
        drawRect(
            color = overlayColor,
            topLeft = Offset.Zero,
            size = androidx.compose.ui.geometry.Size(width, top)
        )
        // 2. Bottom overlay rectangle
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, bottom),
            size = androidx.compose.ui.geometry.Size(width, height - bottom)
        )
        // 3. Left overlay rectangle
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(left, boxSize)
        )
        // 4. Right overlay rectangle
        drawRect(
            color = overlayColor,
            topLeft = Offset(right, top),
            size = androidx.compose.ui.geometry.Size(width - right, boxSize)
        )

        // Draw the four clean neon green focal corners
        val strokeWidth = 5.dp.toPx()
        val cornerLength = 30.dp.toPx()
        val greenColor = Color(0xFF00FFCC)

        // Top-Left corner
        drawLine(greenColor, Offset(left - strokeWidth/2, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(greenColor, Offset(left, top - strokeWidth/2), Offset(left, top + cornerLength), strokeWidth)

        // Top-Right corner
        drawLine(greenColor, Offset(right + strokeWidth/2, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(greenColor, Offset(right, top - strokeWidth/2), Offset(right, top + cornerLength), strokeWidth)

        // Bottom-Left corner
        drawLine(greenColor, Offset(left - strokeWidth/2, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(greenColor, Offset(left, bottom + strokeWidth/2), Offset(left, bottom - cornerLength), strokeWidth)

        // Bottom-Right corner
        drawLine(greenColor, Offset(right + strokeWidth/2, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(greenColor, Offset(right, bottom + strokeWidth/2), Offset(right, bottom - cornerLength), strokeWidth)

        // Scanning Laser beam swept line
        val laserY = top + (boxSize * laserYRatio)
        drawLine(
            color = Color.Red.copy(alpha = 0.8f),
            start = Offset(left + 10.dp.toPx(), laserY),
            end = Offset(right - 10.dp.toPx(), laserY),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestView(
    permissionState: com.google.accompanist.permissions.PermissionState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "จำเป็นต้องใช้สิทธิ์เข้าถึงกล้องถ่ายภาพ",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (permissionState.status.shouldShowRationale) {
                "แอปพลิเคชันระบบต้องการขอใช้กล้องอุปกรณ์ของคุณ เพื่อทำหน้าที่สแกนป้าย QR Code / แท็กอุปกรณ์ของชิ้นส่วนเครื่องบิน"
            } else {
                "กรุณาอนุญาตสิทธิ์เข้าถึงกล้องอุปกรณ์ของคุณเพื่อเริ่มใช้งานการสแกนรหัสอุปกรณ์"
            },
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ยกเลิก")
            }
            Button(
                onClick = { permissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("อนุญาตสิทธิ์กล้อง")
            }
        }
    }
}
