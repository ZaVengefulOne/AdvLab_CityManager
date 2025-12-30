package org.vengeful.citymanager.uikit.composables.news

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jetbrains.skia.Bitmap
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.jetbrains.skia.Image as SkiaImage


@Composable
fun AsyncNewsImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }
    var error by remember(imageUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isBlank()) {
            error = "Empty image URL"
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        error = null
        val client = HttpClient()
        try {
            val response = client.get(imageUrl) {
                // Указываем, что ожидаем бинарные данные
            }

            // Проверяем статус ответа
            if (response.status != HttpStatusCode.OK) {
                throw Exception("HTTP error: ${response.status}")
            }

            // Читаем байты через HttpStatement
            val bytes = when (response) {
                is HttpStatement -> {
                    response.execute().readRawBytes()
                }
                else -> {
                    response.body<ByteArray>()
                }
            }

            // Проверяем, что данные не пустые
            if (bytes.isEmpty()) {
                throw Exception("Empty image data")
            }

            // Проверяем первые байты для определения формата
            val isPng = bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte()
            val isJpg = bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte()

            if (!isPng && !isJpg) {
                throw Exception("Invalid image format. Expected PNG or JPG")
            }

            val skiaImage = SkiaImage.makeFromEncoded(bytes)
            val bitmap = Bitmap.makeFromImage(skiaImage)
            imageBitmap = bitmap.asComposeImageBitmap()
        } catch (e: Exception) {
            error = e.message ?: "Unknown error: ${e.javaClass.simpleName}"
            e.printStackTrace()
        } finally {
            try {
                client.close()
            } catch (e: Exception) {
                // Игнорируем ошибки при закрытии
            }
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                VengText(
                    text = "Ошибка загрузки изображения: $error",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        imageBitmap != null -> {
            Image(
                painter = BitmapPainter(imageBitmap!!),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
    }
}
