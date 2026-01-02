package org.vengeful.citymanager.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * JVM-специфичная реализация конвертации байтов в ImageBitmap.
 * Использует Skia для desktop.
 */
actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        val skiaImage = SkiaImage.makeFromEncoded(bytes)
        val bitmap = Bitmap.makeFromImage(skiaImage)
        bitmap.asComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}


