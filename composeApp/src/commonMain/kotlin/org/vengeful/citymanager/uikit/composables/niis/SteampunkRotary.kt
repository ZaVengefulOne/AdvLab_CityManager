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
fun SteampunkRotary(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val angle = (value / 100f) * 360f

    var startValue by remember { mutableStateOf(value) }
    var lastAngle by remember { mutableStateOf(0f) }

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
                .pointerInput(value) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startValue = value
                            val center = size.width / 2f
                            val startOffset = offset - Offset(center, center)
                            lastAngle = atan2(startOffset.y, startOffset.x)
                        }
                    ) { change, _ ->
                        val center = size.width / 2f
                        val currentOffset = change.position - Offset(center, center)
                        val currentAngle = atan2(currentOffset.y, currentOffset.x)
                        val delta = currentAngle - lastAngle

                        val normalizedDelta = when {
                            delta > PI.toFloat() -> delta - 2f * PI.toFloat()
                            delta < -PI.toFloat() -> delta + 2f * PI.toFloat()
                            else -> delta
                        }
                        val deltaValue = (normalizedDelta / (2f * PI.toFloat()) * 100f).toInt()
                        val newValue = (startValue + deltaValue).coerceIn(0, 100)
                        onValueChange(newValue)
                        lastAngle = currentAngle
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 15

                // Рисуем деления
                for (i in 0..100 step 10) {
                    val angleRad = Math.toRadians((i / 100f * 360f - 90).toDouble())
                    val startX = center.x + cos(angleRad) * (radius - 8)
                    val startY = center.y + sin(angleRad) * (radius - 8)
                    val endX = center.x + cos(angleRad) * radius
                    val endY = center.y + sin(angleRad) * radius

                    drawLine(
                        color = colorScheme.borderDark,
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = 2f
                    )
                }

                // Рисуем указатель
                val angleRad = Math.toRadians(angle.toDouble() - 90)
                val pointerLength = radius - 5
                val pointerEndX = center.x + cos(angleRad) * pointerLength
                val pointerEndY = center.y + sin(angleRad) * pointerLength

                drawLine(
                    color = colorScheme.borderLight,
                    start = center,
                    end = Offset(pointerEndX.toFloat(), pointerEndY.toFloat()),
                    strokeWidth = 4f
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
