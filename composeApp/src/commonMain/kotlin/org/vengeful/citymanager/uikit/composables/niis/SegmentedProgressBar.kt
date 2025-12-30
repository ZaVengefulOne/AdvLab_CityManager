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
fun SegmentedProgressBar(
    progress: Int,
    totalSegments: Int = 7,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                colorScheme.background.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalSegments) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (index < progress) {
                            Color(0xFF4CAF50)
                        } else {
                            colorScheme.background.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .shadow(
                        elevation = if (index < progress) 4.dp else 0.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (index < progress) {
                            Color(0xFF81C784)
                        } else {
                            colorScheme.borderDark.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

