package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
fun SteampunkLever(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val angle = (value / 100f) * 60f - 30f // От -30 до +30 градусов

    val animatedAngle by animateFloatAsState(
        targetValue = angle,
        animationSpec = tween(200),
        label = "lever_angle"
    )

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
                .width(100.dp)
                .height(120.dp)
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
                    detectDragGestures { change, _ ->
                        val centerY = size.height / 2f
                        val deltaY = change.position.y - centerY
                        val normalizedDelta = (deltaY / size.height * 100).coerceIn(-50f, 50f)
                        val newValue = (50 + normalizedDelta).toInt().coerceIn(0, 100)
                        onValueChange(newValue)
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(80.dp)
                    .rotate(animatedAngle)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                        ),
                        RoundedCornerShape(4.dp)
                    )
                    .shadow(4.dp, RoundedCornerShape(4.dp))
            )
        }

        VengText(
            text = "$value",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


