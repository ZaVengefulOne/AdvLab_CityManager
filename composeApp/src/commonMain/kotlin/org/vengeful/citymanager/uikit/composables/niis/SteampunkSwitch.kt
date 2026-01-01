package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun SteampunkSwitch(
    value: Int, // 0 or 100
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val isOn = value >= 50

    val switchPosition by animateFloatAsState(
        targetValue = if (isOn) 1f else 0f,
        animationSpec = tween(300),
        label = "switch_position"
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
                .width(80.dp)
                .height(40.dp)
                .background(
                    if (isOn) colorScheme.borderLight.copy(alpha = 0.3f) else colorScheme.background.copy(alpha = 0.7f),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clickable {
                    onValueChange(if (isOn) 0 else 100)
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = (switchPosition - 0.5f) * 72.dp)
                    .size(32.dp)
                    .background(
                        colorScheme.borderLight,
                        CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = colorScheme.borderDark,
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape)
            )
        }

        VengText(
            text = if (isOn) "100" else "0",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


