package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Эффект покраснения экрана при перегреве
 * @param overheatProgress Прогресс перегрева (0.0f - 1.0f)
 * @param threshold Порог, при котором начинается эффект (по умолчанию 0.5f = 50%)
 * @param maxIntensity Максимальная интенсивность эффекта (0.0f - 1.0f)
 */
@Composable
fun OverheatEffect(
    overheatProgress: Float,
    modifier: Modifier = Modifier,
    threshold: Float = 0.5f,
    maxIntensity: Float = 0.4f
) {
    // Вычисляем интенсивность эффекта на основе прогресса перегрева
    val intensity = if (overheatProgress > threshold) {
        // Плавное увеличение от threshold до 1.0f
        val normalizedProgress = (overheatProgress - threshold) / (1.0f - threshold)
        min(normalizedProgress * maxIntensity, maxIntensity)
    } else {
        0f
    }

    if (intensity > 0f) {
        Box(modifier = modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2f
                val centerY = height / 2f
                
                // Цвет покраснения с прозрачностью, зависящей от интенсивности
                val redColor = Color(1f, 0f, 0f, intensity * 0.4f) // Красный с прозрачностью
                val darkRedColor = Color(0.6f, 0f, 0f, intensity * 0.6f) // Тёмно-красный для краёв
                
                // Радиус градиента - большой радиус для покрытия всего экрана
                // Чем больше интенсивность, тем сильнее эффект (больше непрозрачность)
                val maxRadius = max(width, height) * 1.5f
                
                // Создаём радиальный градиент от центра с обратным направлением
                // (красный по краям, прозрачный в центре) для эффекта "от краёв к центру"
                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent, // В центре - прозрачный
                        Color.Transparent, // Остаёмся прозрачными ближе к центру
                        redColor.copy(alpha = intensity * 0.15f), // Немного красного
                        darkRedColor.copy(alpha = intensity * 0.5f), // Тёмно-красный
                        darkRedColor // Тёмно-красный по краям
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadius
                )
                
                // Рисуем градиент на весь экран
                drawRect(
                    brush = brush,
                    size = Size(width, height)
                )
            }
        }
    }
}

