package org.vengeful.citymanager.uikit.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun WindowsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colors = SeveritepunkThemes.getColorScheme(theme)
    
    // Цвета в стиле Windows 95/98, но с использованием темы проекта
    val backgroundColor = Color(0xFFC0C0C0) // Светло-серый фон
    val borderDark = Color(0xFF808080) // Темная граница
    val borderLight = Color(0xFFFFFFFF) // Светлая граница
    val progressColor = colors.borderLight // Цвет прогресса из темы
    val progressDark = colors.borderDark.darken(0.3f) // Темный оттенок для объема
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .border(1.dp, borderDark, androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val width = size.width
            val height = size.height
            val progressWidth = width * progress.coerceIn(0f, 1f)
            
            // Рисуем фон прогресс-бара
            drawRect(
                color = backgroundColor,
                topLeft = Offset(0f, 0f),
                size = Size(width, height)
            )
            
            // Рисуем прогресс (с эффектом объема)
            if (progressWidth > 0) {
                // Основной цвет прогресса
                drawRect(
                    color = progressColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(progressWidth, height)
                )
                
                // Верхняя светлая граница для объема
                drawLine(
                    color = borderLight,
                    start = Offset(0f, 0f),
                    end = Offset(progressWidth, 0f),
                    strokeWidth = 1f
                )
                
                // Левая светлая граница для объема
                drawLine(
                    color = borderLight,
                    start = Offset(0f, 0f),
                    end = Offset(0f, height),
                    strokeWidth = 1f
                )
                
                // Нижняя темная граница для объема
                drawLine(
                    color = progressDark,
                    start = Offset(0f, height - 1f),
                    end = Offset(progressWidth, height - 1f),
                    strokeWidth = 1f
                )
                
                // Правая темная граница для объема
                drawLine(
                    color = progressDark,
                    start = Offset(progressWidth - 1f, 0f),
                    end = Offset(progressWidth - 1f, height),
                    strokeWidth = 1f
                )
            }
            
            // Внешняя рамка (темная)
            drawRect(
                color = borderDark,
                topLeft = Offset(0f, 0f),
                size = Size(width, height),
                style = Stroke(width = 1f)
            )
        }
    }
}

private fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

