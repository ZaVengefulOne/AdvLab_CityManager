package org.vengeful.citymanager.uikit.composables.veng

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.darken

@Composable
fun VengButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    enabled: Boolean = true,
    padding: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 2.dp,
    theme: ColorTheme = ColorTheme.GOLDEN,
    isIconButton: Boolean = false,
    content: @Composable (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonColors = remember(enabled, isPressed, theme) {
        val scheme = SeveritepunkThemes.getColorScheme(theme)
        if (!enabled) {
            scheme.copy(
                background = scheme.background.copy(alpha = 0.5f),
                text = scheme.text.copy(alpha = 0.5f)
            )
        } else if (isPressed) {
            scheme.copy(
                background = scheme.background.darken(0.2f),
                borderLight = scheme.borderLight.darken(0.2f)
            )
        } else {
            scheme
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(buttonColors.background)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(buttonColors.borderLight, buttonColors.borderDark)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enabled) onClick()
            }
            .drawBehind {
                drawCircle(
                    color = buttonColors.rivets,
                    radius = 4f,
                    center = Offset(8f, 8f)
                )
                drawCircle(
                    color = buttonColors.rivets,
                    radius = 4f,
                    center = Offset(size.width - 8f, 8f)
                )
                drawCircle(
                    color = buttonColors.rivets,
                    radius = 4f,
                    center = Offset(8f, size.height - 8f)
                )
                drawCircle(
                    color = buttonColors.rivets,
                    radius = 4f,
                    center = Offset(size.width - 8f, size.height - 8f)
                )
            }
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        if (!isIconButton) {
            if (content != null) {
                content()
            } else {
                VengText(
                    text = text,
                    color = buttonColors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (content != null) {
                    content()
                }
                VengText(
                    text = text,
                    color = buttonColors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

    }
}



