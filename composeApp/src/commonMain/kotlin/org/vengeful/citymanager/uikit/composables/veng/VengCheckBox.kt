package org.vengeful.citymanager.uikit.composables.veng

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun VengCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkboxSize: Dp = 24.dp,
    cornerRadius: Dp = 4.dp,
    borderWidth: Dp = 2.dp,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val checkboxColors = remember(enabled, checked, theme) {
        val scheme = SeveritepunkThemes.getColorScheme(theme)
        if (!enabled) {
            CheckboxColors(
                background = scheme.background.copy(alpha = 0.5f),
                borderLight = scheme.borderLight.copy(alpha = 0.5f),
                borderDark = scheme.borderDark.copy(alpha = 0.5f),
                checkmark = scheme.text.copy(alpha = 0.5f),
                rivets = scheme.rivets.copy(alpha = 0.5f)
            )
        } else if (checked) {
            CheckboxColors(
                background = scheme.borderLight.copy(alpha = 0.3f),
                borderLight = scheme.borderLight,
                borderDark = scheme.borderDark,
                checkmark = scheme.borderLight,
                rivets = scheme.rivets
            )
        } else {
            CheckboxColors(
                background = scheme.background,
                borderLight = scheme.borderLight,
                borderDark = scheme.borderDark,
                checkmark = scheme.borderLight,
                rivets = scheme.rivets
            )
        }
    }

    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "checkmark_scale"
    )

    Box(
        modifier = modifier
            .size(checkboxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(checkboxColors.background)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(checkboxColors.borderLight, checkboxColors.borderDark)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .shadow(
                elevation = if (checked) 4.dp else 2.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enabled) {
                    onCheckedChange(!checked)
                }
            }
            .drawBehind {
                // Декоративные заклёпки в углах
                val rivetRadius = 2f
                drawCircle(
                    color = checkboxColors.rivets,
                    radius = rivetRadius,
                    center = Offset(rivetRadius + 2f, rivetRadius + 2f)
                )
                drawCircle(
                    color = checkboxColors.rivets,
                    radius = rivetRadius,
                    center = Offset(size.width - rivetRadius - 2f, rivetRadius + 2f)
                )
                drawCircle(
                    color = checkboxColors.rivets,
                    radius = rivetRadius,
                    center = Offset(rivetRadius + 2f, size.height - rivetRadius - 2f)
                )
                drawCircle(
                    color = checkboxColors.rivets,
                    radius = rivetRadius,
                    center = Offset(size.width - rivetRadius - 2f, size.height - rivetRadius - 2f)
                )

                // Галочка
                if (checkmarkScale > 0f) {
                    val checkmarkPath = Path().apply {
                        val startX = size.width * 0.2f
                        val startY = size.height * 0.5f
                        val midX = size.width * 0.4f
                        val midY = size.height * 0.7f
                        val endX = size.width * 0.8f
                        val endY = size.height * 0.3f

                        moveTo(startX, startY)
                        lineTo(midX, midY)
                        lineTo(endX, endY)
                    }

                    drawPath(
                        path = checkmarkPath,
                        color = checkboxColors.checkmark,
                        style = Stroke(
                            width = 3f * checkmarkScale,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center,
        propagateMinConstraints = false,
        content = { }
    )
}

private data class CheckboxColors(
    val background: Color,
    val borderLight: Color,
    val borderDark: Color,
    val checkmark: Color,
    val rivets: Color
)
