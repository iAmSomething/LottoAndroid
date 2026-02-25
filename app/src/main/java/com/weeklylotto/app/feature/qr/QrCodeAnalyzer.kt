package com.weeklylotto.app.feature.qr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class QrCodeAnalyzer(
    private val onDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {
    private val options =
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

    private val scanner = BarcodeScanning.getClient(options)
    private val processing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val inputImage =
            InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees,
            )

        scanner
            .process(inputImage)
            .addOnSuccessListener { barcodes ->
                val value =
                    barcodes
                        .firstOrNull { !it.rawValue.isNullOrBlank() }
                        ?.rawValue
                        ?.trim()

                if (!value.isNullOrBlank()) {
                    onDetected(value)
                }
            }.addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }

    override fun close() {
        scanner.close()
    }
}
