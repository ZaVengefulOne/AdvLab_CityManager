package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import kotlin.math.*

@Composable
fun SteampunkDial(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val angle = (value / 100f) * 360f - 90f

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
                    CircleShape
                )
                .border(
                    width = 3.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = CircleShape
                )
                .shadow(6.dp, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = size.width / 2f
                        val offset = change.position - Offset(center, center)
                        val angleRad = atan2(offset.y, offset.x) + PI / 2
                        val normalizedAngle = (angleRad / (2 * PI) + 1f) % 1f
                        val newValue = (normalizedAngle * 100).toInt().coerceIn(0, 100)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 20

                // Рисуем деления
                for (i in 0..100 step 10) {
                    val angleRad = Math.toRadians((i / 100f * 360f - 90).toDouble())
                    val startX = center.x + cos(angleRad) * (radius - 10)
                    val startY = center.y + sin(angleRad) * (radius - 10)
                    val endX = center.x + cos(angleRad) * radius
                    val endY = center.y + sin(angleRad) * radius

                    drawLine(
                        color = colorScheme.borderDark,
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = 2f
                    )
                }

                // Рисуем стрелку
                val angleRad = Math.toRadians(angle.toDouble())
                val arrowLength = radius - 15
                val arrowEndX = center.x + cos(angleRad) * arrowLength
                val arrowEndY = center.y + sin(angleRad) * arrowLength

                drawLine(
                    color = colorScheme.borderLight,
                    start = center,
                    end = Offset(arrowEndX.toFloat(), arrowEndY.toFloat()),
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


