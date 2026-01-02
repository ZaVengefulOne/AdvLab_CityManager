package org.vengeful.citymanager.utils

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Конвертирует байты изображения в ImageBitmap.
 * Платформо-специфичная реализация.
 */
expect fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap?


