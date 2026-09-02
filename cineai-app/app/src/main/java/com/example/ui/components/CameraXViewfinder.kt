package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Controller holder for CameraX active sessions.
 */
class CameraXController(
    val context: Context
) {
    var imageCapture: ImageCapture? = null
    var camera: Camera? = null
    var isInitialized by mutableStateOf(false)
    var initializationError by mutableStateOf<String?>(null)

    fun takePicture(
        flashMode: Int = ImageCapture.FLASH_MODE_AUTO,
        onPhotoCaptured: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture
        if (capture == null) {
            onError(IllegalStateException("CameraX ImageCapture is not ready"))
            return
        }

        try {
            capture.flashMode = flashMode
        } catch (e: Exception) {
            Log.w("CameraXViewfinder", "Could not set flash mode: ${e.message}")
        }

        val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val photoFile = File(imagesDir, "CINE_RAW_${timeStamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val executor = ContextCompat.getMainExecutor(context)

        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun setTorch(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }
}

@Composable
fun CameraXViewfinder(
    modifier: Modifier = Modifier,
    cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
    flashMode: Int = ImageCapture.FLASH_MODE_AUTO,
    isTorchOn: Boolean = false,
    onControllerReady: (CameraXController) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val controller = remember { CameraXController(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Update Torch
    LaunchedEffect(isTorchOn, controller.camera) {
        controller.setTorch(isTorchOn)
    }

    // Bind Camera Provider when lens or preview changes
    LaunchedEffect(cameraLensFacing, previewView) {
        val view = previewView ?: return@LaunchedEffect
        try {
            val cameraProvider = getCameraProvider(context)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraLensFacing)
                .build()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .build()

            cameraProvider.unbindAll()

            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            controller.imageCapture = imageCapture
            controller.camera = camera
            controller.isInitialized = true
            controller.initializationError = null
            onControllerReady(controller)

        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Camera hardware initialization failed"
            controller.initializationError = msg
            controller.isInitialized = false
            onError(msg)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val executor = ContextCompat.getMainExecutor(context)
                ProcessCameraProvider.getInstance(context).addListener({
                    try {
                        ProcessCameraProvider.getInstance(context).get().unbindAll()
                    } catch (e: Exception) {
                        // ignore
                    }
                }, executor)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                continuation.resume(future.get())
            } catch (e: Exception) {
                continuation.cancel(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
