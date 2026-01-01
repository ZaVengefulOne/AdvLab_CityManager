package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun OverheatProgressBar(
    progress: Float, // 0.0f - 1.0f
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val darkRed = Color(0xFF8B0000) // Тёмно-красный цвет
    val brightRed = Color(0xFFDC143C) // Более яркий красный для градиента

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(
                colorScheme.background.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(darkRed, brightRed, darkRed)
                    ),
                    RoundedCornerShape(8.dp)
                )
                .shadow(
                    elevation = if (progress > 0f) 4.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (progress > 0.8f) Color(0xFFFF0000) else Color(0xFF8B0000),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

