package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.SettingsManager
import com.example.data.WatermarkConfig
import com.example.ui.components.WatermarkOverlay
import com.example.ui.dialogs.EditRemarksDialog
import com.example.ui.dialogs.EditWatermarkDialog
import com.example.util.LocationHelper
import com.example.util.WatermarkRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Main Camera viewfinder screen with real-time watermark overlay, controls, and capture engine.
 */
@Composable
fun CameraScreen(
    settingsManager: SettingsManager,
    onNavigateToSettings: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onPhotoCaptured: (imagePath: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    // Active watermark configuration for current shooting session
    var config by remember { mutableStateOf(settingsManager.getDefaultConfig()) }

    // Live Clock State (updates every second)
    var currentTimeStr by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
    }

    LaunchedEffect(config.autoTime) {
        while (true) {
            if (config.autoTime) {
                currentTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            }
            delay(1000)
        }
    }

    // Camera State
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var showGrid by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var showRemarksDialog by remember { mutableStateOf(false) }

    // Permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPermission

        if (hasLocationPermission && config.autoGps) {
            coroutineScope.launch {
                val loc = locationHelper.getCurrentLocation()
                if (loc != null) {
                    config = config.copy(
                        longitude = String.format(Locale.US, "%.6f", loc.longitude),
                        latitude = String.format(Locale.US, "%.6f", loc.latitude),
                        altitude = String.format(Locale.US, "%.1f米", loc.altitude),
                        address = if (config.autoAddress && !loc.address.isNullOrBlank()) loc.address else config.address
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (config.autoGps) {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                config = config.copy(
                    longitude = String.format(Locale.US, "%.6f", loc.longitude),
                    latitude = String.format(Locale.US, "%.6f", loc.latitude),
                    altitude = String.format(Locale.US, "%.1f米", loc.altitude),
                    address = if (config.autoAddress && !loc.address.isNullOrBlank()) loc.address else config.address
                )
            }
        }
    }

    // Single photo picker to add watermark to existing photos from album
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isCapturing = true
            coroutineScope.launch {
                val bitmap = WatermarkRenderer.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val watermarked = WatermarkRenderer.drawWatermark(bitmap, config, burnHintBadge = false)
                    val savedFile = WatermarkRenderer.saveWatermarkedBitmap(context, watermarked)
                    if (savedFile != null) {
                        Toast.makeText(context, "水印添加成功！", Toast.LENGTH_SHORT).show()
                        onPhotoCaptured(savedFile.absolutePath)
                    } else {
                        Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "无法加载所选图片", Toast.LENGTH_SHORT).show()
                }
                isCapturing = false
            }
        }
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission request placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "需要相机权限以进行现场拍照",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "拍摄带有实时经纬度、地址与防伪水印的工程及现场照片",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.testTag("request_permission_button")
                ) {
                    Text("授予权限并开始使用")
                }
            }
        } else {
            // Camera Preview View
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .build()

                        imageCapture = capture

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    imageCapture?.flashMode = flashMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Optional 3x3 Grid Overlay
            if (showGrid) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val gridColor = Color.White.copy(alpha = 0.25f)
                    // Vertical lines
                    drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 1.dp.toPx())
                    // Horizontal lines
                    drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 1.dp.toPx())
                }
            }

            // Real-time Watermark Overlay (matching user reference image)
            WatermarkOverlay(
                config = config,
                currentTime = if (config.autoTime) currentTimeStr else config.getEffectiveTime(),
                showHintBadge = config.showHintBadge,
                onEditWatermarkClick = { showEditDialog = true },
                onEditRemarksClick = { showRemarksDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 110.dp) // Leave space for camera shutter bottom bar
            )

            // Top Control Bar (Translucent)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 36.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                        imageCapture?.flashMode = flashMode
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("flash_toggle_button")
                ) {
                    val icon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    }
                    Icon(icon, contentDescription = "闪光灯", tint = Color.White)
                }

                // Grid Toggle
                IconButton(
                    onClick = { showGrid = !showGrid },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (showGrid) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "参考线", tint = Color.White)
                }

                // GPS Refresh
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val loc = locationHelper.getCurrentLocation()
                            if (loc != null) {
                                config = config.copy(
                                    longitude = String.format(Locale.US, "%.6f", loc.longitude),
                                    latitude = String.format(Locale.US, "%.6f", loc.latitude),
                                    altitude = String.format(Locale.US, "%.1f米", loc.altitude),
                                    address = if (!loc.address.isNullOrBlank()) loc.address else config.address
                                )
                                Toast.makeText(context, "GPS与地址已更新", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "已使用配置的默认经纬度", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("refresh_location_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "定位", tint = Color.White)
                }

                // Camera Switch (Front/Back)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("switch_camera_button")
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = "翻转镜头", tint = Color.White)
                }

                // Settings Page Button (To configure defaults)
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("open_settings_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "设置默认值", tint = Color.White)
                }
            }

            // Bottom Shutter & Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Watermark Album
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("open_gallery_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "水印相册", tint = Color.White)
                    }

                    // Center: Shutter Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) Color.Gray else Color.White)
                            .clickable(enabled = !isCapturing && imageCapture != null) {
                                isCapturing = true
                                val capture = imageCapture ?: return@clickable

                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                            val buffer = imageProxy.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            imageProxy.close()

                                            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                            val rotated = Bitmap.createBitmap(
                                                originalBitmap,
                                                0,
                                                0,
                                                originalBitmap.width,
                                                originalBitmap.height,
                                                matrix,
                                                true
                                            )

                                            // Burn watermark
                                            val watermarked = WatermarkRenderer.drawWatermark(
                                                rotated,
                                                config.copy(
                                                    time = if (config.autoTime) currentTimeStr else config.getEffectiveTime()
                                                ),
                                                burnHintBadge = false
                                            )

                                            coroutineScope.launch {
                                                val file = WatermarkRenderer.saveWatermarkedBitmap(context, watermarked)
                                                isCapturing = false
                                                if (file != null) {
                                                    onPhotoCaptured(file.absolutePath)
                                                } else {
                                                    Toast.makeText(context, "保存照片失败", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                            coroutineScope.launch {
                                                isCapturing = false
                                                Toast.makeText(context, "拍照失败: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                            .testTag("shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    // Right: Add Watermark to existing Photo from Album
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(4.dp)
                            .testTag("pick_from_album_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "导入照片",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "导入加水印",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Full Edit Watermark Dialog
        if (showEditDialog) {
            EditWatermarkDialog(
                currentConfig = config,
                onDismiss = { showEditDialog = false },
                onFetchCurrentGps = {
                    coroutineScope.launch {
                        val loc = locationHelper.getCurrentLocation()
                        if (loc != null) {
                            config = config.copy(
                                longitude = String.format(Locale.US, "%.6f", loc.longitude),
                                latitude = String.format(Locale.US, "%.6f", loc.latitude),
                                altitude = String.format(Locale.US, "%.1f米", loc.altitude),
                                address = if (!loc.address.isNullOrBlank()) loc.address else config.address
                            )
                            Toast.makeText(context, "已获取最新GPS坐标", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "未获取到GPS，保持原值", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onConfirm = { updatedConfig, saveAsDefault ->
                    config = updatedConfig
                    if (saveAsDefault) {
                        settingsManager.saveDefaultConfig(updatedConfig)
                        Toast.makeText(context, "已同时保存为默认水印！", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "水印已更新", Toast.LENGTH_SHORT).show()
                    }
                    showEditDialog = false
                }
            )
        }

        // Quick Remarks Edit Dialog
        if (showRemarksDialog) {
            EditRemarksDialog(
                initialRemarks = config.remarks,
                onDismiss = { showRemarksDialog = false },
                onConfirm = { newRemarks, saveAsDefault ->
                    config = config.copy(remarks = newRemarks)
                    if (saveAsDefault) {
                        settingsManager.saveDefaultConfig(config)
                        Toast.makeText(context, "已设为默认备注！", Toast.LENGTH_SHORT).show()
                    }
                    showRemarksDialog = false
                }
            )
        }
    }
}
