package org.vengeful.citymanager.uikit.composables.veng

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.darken

@Composable
fun VengTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 2.dp,
    padding: Dp = 12.dp
) {
    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.background)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(colors.borderLight, colors.borderDark)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .shadow(4.dp, RoundedCornerShape(cornerRadius))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, tabText ->
            val isSelected = index == selectedTabIndex
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(cornerRadius - 2.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(colors.borderLight, colors.borderDark)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(colors.background, cardColors.background)
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        brush = if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(colors.borderLight, colors.borderDark)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(colors.borderDark.copy(alpha = 0.5f), colors.borderDark.copy(alpha = 0.3f))
                            )
                        },
                        shape = RoundedCornerShape(cornerRadius - 2.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onTabSelected(index)
                    }
                    .drawBehind {
                        if (isSelected) {
                            // Риветы для выбранной вкладки
                            drawCircle(
                                color = colors.rivets,
                                radius = 3f,
                                center = Offset(6f, 6f)
                            )
                            drawCircle(
                                color = colors.rivets,
                                radius = 3f,
                                center = Offset(size.width - 6f, 6f)
                            )
                            drawCircle(
                                color = colors.rivets,
                                radius = 3f,
                                center = Offset(6f, size.height - 6f)
                            )
                            drawCircle(
                                color = colors.rivets,
                                radius = 3f,
                                center = Offset(size.width - 6f, size.height - 6f)
                            )
                        }
                    }
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                VengText(
                    text = tabText,
                    color = if (isSelected) colors.text else colors.text.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

