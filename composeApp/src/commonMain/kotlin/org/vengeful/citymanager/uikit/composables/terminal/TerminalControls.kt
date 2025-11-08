package org.vengeful.citymanager.uikit.composables.terminal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun TerminalControls(
    modifier: Modifier = Modifier,
    onShutdown: () -> Unit,
    onRestart: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN,
) {
    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SteampunkControlButton(
            onClick = onRestart,
            icon = "🔄",
            text = "ПЕРЕГРУЗКА",
            accentColor = colors.rivets,
            theme = theme,
            modifier = modifier
        )

        SteampunkControlButton(
            onClick = onShutdown,
            icon = "⏻",
            text = "ВЫКЛЮЧЕНИЕ",
            accentColor = colors.borderLight,
            theme = theme,
            modifier = modifier
        )
    }
}

@Composable
fun SteampunkControlButton(
    onClick: () -> Unit,
    icon: String,
    text: String,
    accentColor: Color,
    theme: ColorTheme = ColorTheme.GOLDEN,
    modifier: Modifier = Modifier
) {
    var isAnimating by remember { mutableStateOf(false) }
    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }

    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "button_rotation"
    )

    val pulse by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

    Box(
        modifier = modifier
            .scale(pulse)
            .clickable {
                isAnimating = true
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    isAnimating = false
                    onClick()
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Стилизованная кнопка в стиле стимпанк
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(colors.background)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(accentColor, colors.borderDark)
                        ),
                        shape = CircleShape
                    )
                    .graphicsLayer {
                        rotationZ = rotation
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Внутренний круг с иконкой
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(accentColor, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                // Декоративные заклёпки
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(6.dp)
                        .background(colors.rivets, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .background(colors.rivets, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(6.dp)
                        .background(colors.rivets, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(6.dp)
                        .background(colors.rivets, CircleShape)
                )
            }

            // Текст под кнопкой
            Text(
                text = text,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}