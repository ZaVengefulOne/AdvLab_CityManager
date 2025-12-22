package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun SteampunkWheel(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val angle = (value / 100f) * 360f - 90f
    
    // Выносим remember наружу из pointerInput
    var startValue by remember { mutableStateOf(value) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label.isNotEmpty()) {
            VengText(
                text = label,
                color = colorScheme.text,
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    colorScheme.background.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startValue = value
                        }
                    ) { change, dragAmount ->
                        // Используем вертикальное движение для плавного изменения значения
                        // Вычисляем новое значение на основе текущей позиции курсора
                        val centerY = size.height / 2f
                        val relativeY = (change.position.y - centerY) / size.height
                        // Инвертируем, чтобы движение вверх увеличивало значение
                        val normalizedValue = (0.5f - relativeY).coerceIn(-0.5f, 0.5f) * 2f
                        val newValue = ((normalizedValue + 1f) / 2f * 100f).toInt().coerceIn(0, 100)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 10

                // Рисуем внешнее кольцо
                drawCircle(
                    color = colorScheme.borderDark,
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )

                // Рисуем деления
                for (i in 0..100 step 5) {
                    val angleRad = Math.toRadians((i / 100f * 360f - 90).toDouble())
                    val isMajor = i % 10 == 0
                    val lineLength = if (isMajor) 8f else 4f
                    val lineStart = radius - lineLength
                    
                    val startX = center.x + cos(angleRad) * lineStart
                    val startY = center.y + sin(angleRad) * lineStart
                    val endX = center.x + cos(angleRad) * radius
                    val endY = center.y + sin(angleRad) * radius

                    drawLine(
                        color = if (isMajor) colorScheme.borderLight else colorScheme.borderDark,
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = if (isMajor) 2f else 1f
                    )
                }

                // Рисуем индикатор текущего значения
                val indicatorAngle = Math.toRadians(angle.toDouble())
                val indicatorLength = radius - 5
                val indicatorEndX = center.x + cos(indicatorAngle) * indicatorLength
                val indicatorEndY = center.y + sin(indicatorAngle) * indicatorLength

                drawLine(
                    color = colorScheme.borderLight,
                    start = center,
                    end = Offset(indicatorEndX.toFloat(), indicatorEndY.toFloat()),
                    strokeWidth = 4f
                )

                // Рисуем центральный круг
                drawCircle(
                    color = colorScheme.borderLight.copy(alpha = 0.5f),
                    radius = 8f,
                    center = center
                )
            }
        }

        VengText(
            text = "$value",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
