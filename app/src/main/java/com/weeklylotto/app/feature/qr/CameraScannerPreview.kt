package com.weeklylotto.app.feature.qr

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

@Composable
fun CameraScannerPreview(
    modifier: Modifier = Modifier,
    onQrDetected: (String) -> Unit,
    torchEnabled: Boolean = false,
    onTorchAvailabilityChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnQrDetected by rememberUpdatedState(onQrDetected)
    val latestOnTorchAvailabilityChanged by rememberUpdatedState(onTorchAvailabilityChanged)
    val previewView =
        remember {
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember { QrCodeAnalyzer(onDetected = { value -> latestOnQrDetected(value) }) }
    var boundCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    DisposableEffect(lifecycleOwner, analyzer) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        val bindingRunnable =
            Runnable {
                if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@Runnable
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val analysis =
                    ImageAnalysis
                        .Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer(cameraExecutor, analyzer)
                        }

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }.onSuccess { camera ->
                    boundCamera = camera
                    val hasTorch = camera.cameraInfo.hasFlashUnit()
                    latestOnTorchAvailabilityChanged(hasTorch)
                    if (hasTorch) {
                        camera.cameraControl.enableTorch(torchEnabled)
                    }
                }.onFailure {
                    boundCamera = null
                    latestOnTorchAvailabilityChanged(false)
                }
            }

        cameraProviderFuture.addListener(bindingRunnable, mainExecutor)

        onDispose {
            boundCamera = null
            runCatching {
                cameraProviderFuture.get().unbindAll()
            }
            analyzer.close()
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(torchEnabled, boundCamera) {
        val camera = boundCamera ?: return@LaunchedEffect
        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(torchEnabled)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}
