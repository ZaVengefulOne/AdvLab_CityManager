package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
fun SteampunkKnob(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val rotation = (value / 100f) * 360f

    // Сохраняем начальное значение для расчета дельты
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
                .size(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorScheme.borderLight.copy(alpha = 0.8f),
                            colorScheme.background.copy(alpha = 0.9f)
                        )
                    ),
                    CircleShape
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = CircleShape
                )
                .shadow(6.dp, CircleShape)
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

                // Рисуем метку
                val markerLength = size.minDimension / 2 - 10
                val angleRad = Math.toRadians(rotation.toDouble() - 90)
                val markerEndX = center.x + cos(angleRad) * markerLength
                val markerEndY = center.y + sin(angleRad) * markerLength

                drawLine(
                    color = colorScheme.borderDark,
                    start = center,
                    end = Offset(markerEndX.toFloat(), markerEndY.toFloat()),
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


