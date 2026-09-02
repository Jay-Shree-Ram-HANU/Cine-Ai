package com.example.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.data.model.PresetEntity
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.CameraGridOverlay
import com.example.ui.components.CameraShutterButton
import com.example.ui.components.CameraXController
import com.example.ui.components.CameraXViewfinder
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineGreenFocus
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CinePrimaryContainer
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(
    viewModel: StudioViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets by viewModel.allPresets.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val context = LocalContext.current

    // Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission needed to capture photos", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraLensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var cameraXController by remember { mutableStateOf<CameraXController?>(null) }
    var cameraXError by remember { mutableStateOf<String?>(null) }
    var showFlashEffect by remember { mutableStateOf(false) }

    // Auto-hide the shutter flash effect shortly after it triggers, otherwise the
    // white overlay would cover the viewfinder permanently.
    LaunchedEffect(showFlashEffect) {
        if (showFlashEffect) {
            delay(220)
            showFlashEffect = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importPhotoFromGallery(context, it) { newId ->
                onNavigateToEditor(newId)
            }
        }
    }

    var selectedPreset by remember {
        mutableStateOf(presets.firstOrNull() ?: PresetEntity(
            id = "teal_orange",
            name = "Teal & Orange Blockbuster",
            category = "Hollywood",
            description = "Hollywood grade",
            unblurStrength = 75f
        ))
    }
    var gridEnabled by remember { mutableStateOf(false) }
    var letterboxRatio by remember { mutableStateOf("2.39:1") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_screen")
    ) {
        // 1. Live Camera Viewfinder Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp, top = 56.dp)
        ) {
            if (hasCameraPermission) {
                CameraXViewfinder(
                    cameraLensFacing = cameraLensFacing,
                    flashMode = flashMode,
                    isTorchOn = false,
                    onControllerReady = { ctrl ->
                        cameraXController = ctrl
                        cameraXError = null
                    },
                    onError = { err ->
                        cameraXError = err
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Permission Request Fallback View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CineObsidian),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = CineGoldPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant camera permission to take photos and transform them into cinematic stills.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CineTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineGoldPrimary,
                                contentColor = Color(0xFF21005D)
                            ),
                            shape = CircleShape
                        ) {
                            Text("Grant Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Grid Overlay
            if (gridEnabled) {
                CameraGridOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Clean Center Focus Indicator
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center)
                    .border(1.5.dp, CineGreenFocus.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            )

            // Flash effect animation
            AnimatedVisibility(
                visible = showFlashEffect,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }

            // Ingestion / Processing spinner indicator
            if (isAiProcessing) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CineGoldPrimary),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CineGoldPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage ?: "Processing Cinematic Frame...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2. Simplified Top Pro Bar (No clutter, clear touch targets)
        Surface(
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                    }
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash Mode",
                        tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) CineGoldPrimary else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Aspect Ratio Pill
                Surface(
                    color = CineSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        letterboxRatio = if (letterboxRatio == "2.39:1") "16:9" else "2.39:1"
                    }
                ) {
                    Text(
                        text = letterboxRatio,
                        color = CineGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Grid Toggle
                IconButton(onClick = { gridEnabled = !gridEnabled }) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Grid",
                        tint = if (gridEnabled) CineGreenFocus else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Flip Camera
                IconButton(
                    onClick = {
                        cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 3. Bottom Camera Controls & LUT Strip
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            CineObsidian.copy(alpha = 0.90f),
                            CineObsidian
                        )
                    )
                )
                .padding(bottom = 78.dp)
        ) {
            // Live Filter Presets Strip
            Text(
                text = "CINEMATIC STYLE: ${selectedPreset.name.uppercase()}",
                color = CineGoldPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets, key = { it.id }) { preset ->
                    val isSelected = selectedPreset.id == preset.id
                    Surface(
                        color = if (isSelected) CineGoldPrimary else CineSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CineGoldPrimary else Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .clickable { selectedPreset = preset }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = preset.name,
                            color = if (isSelected) Color(0xFF21005D) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Shutter Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Import from Gallery Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(50.dp)
                            .background(CineSurfaceVariant, CircleShape)
                            .testTag("camera_import_gallery_btn")
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Import Photo",
                            tint = CineTealAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Import", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }

                // Shutter Button
                CameraShutterButton(
                    isRecording = false,
                    isShortMode = false,
                    modifier = Modifier.testTag("camera_shutter_btn"),
                    onClick = {
                        showFlashEffect = true
                        val ctrl = cameraXController
                        if (hasCameraPermission && ctrl != null && ctrl.isInitialized) {
                            ctrl.takePicture(
                                flashMode = flashMode,
                                onPhotoCaptured = { photoFile ->
                                    viewModel.saveCameraXCapturedPhoto(
                                        photoFile = photoFile,
                                        preset = selectedPreset,
                                        letterboxRatio = letterboxRatio,
                                        autoUnblurEnabled = true,
                                        ev = 0f
                                    ) { newId ->
                                        onNavigateToEditor(newId)
                                    }
                                },
                                onError = { _ ->
                                    // Fallback to demo photo
                                    val newId = viewModel.createAndSaveNewCapture(
                                        title = "Captured Frame",
                                        type = MediaType.PHOTO,
                                        sampleKey = "landscape",
                                        preset = selectedPreset,
                                        fps = 24
                                    )
                                    onNavigateToEditor(newId)
                                }
                            )
                        } else {
                            val newId = viewModel.createAndSaveNewCapture(
                                title = "Captured Frame",
                                type = MediaType.PHOTO,
                                sampleKey = "landscape",
                                preset = selectedPreset,
                                fps = 24
                            )
                            onNavigateToEditor(newId)
                        }
                    }
                )

                // Media Vault Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier
                            .size(50.dp)
                            .background(CineSurfaceVariant, CircleShape)
                            .testTag("camera_vault_nav_btn")
                    ) {
                        Icon(
                            Icons.Default.Collections,
                            contentDescription = "Media Vault",
                            tint = CineGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Vault", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
