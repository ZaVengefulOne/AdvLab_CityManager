package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun SteampunkGauge(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val angle = (value / 100f) * 180f - 90f

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
                .size(120.dp, 80.dp)
                .background(
                    colorScheme.background.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY: Float = size.height.toFloat()
                        val offset = tapOffset - Offset(centerX, centerY)
                        val angleRad = atan2(-offset.y, offset.x)
                        val normalizedAngle = ((angleRad + PI / 2) / PI).coerceIn(0.0, 1.0)
                        val newValue = (normalizedAngle * 100).toInt().coerceIn(0, 100)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height
                val radius = size.width * 0.8f

                // Рисуем дугу
                val path = Path()
                val rect = Rect(
                    left = centerX - radius,
                    top = centerY - radius,
                    right = centerX + radius,
                    bottom = centerY + radius
                )
                path.addArc(
                    oval = rect,
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 180f
                )

                drawPath(
                    path = path,
                    color = colorScheme.borderDark.copy(alpha = 0.5f),
                    style = Stroke(width = 8f)
                )

                // Рисуем активную часть
                val activePath = Path()
                val activeSweep = (value / 100f) * 180f
                activePath.addArc(
                    oval = rect,
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = activeSweep
                )

                drawPath(
                    path = activePath,
                    color = colorScheme.borderLight,
                    style = Stroke(width = 8f)
                )

                // Рисуем стрелку (исправлено)
                val angleRad = Math.toRadians(angle.toDouble())
                val arrowLength = radius * 0.7f
                val arrowStartX = centerX.toFloat()
                val arrowStartY = centerY.toFloat()
                val arrowEndX = arrowStartX + cos(angleRad).toFloat() * arrowLength
                val arrowEndY = arrowStartY + sin(angleRad).toFloat() * arrowLength

                drawLine(
                    color = colorScheme.borderLight,
                    start = Offset(arrowStartX, arrowStartY),
                    end = Offset(arrowEndX, arrowEndY),
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
