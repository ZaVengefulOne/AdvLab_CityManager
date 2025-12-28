package org.vengeful.citymanager.uikit.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.vengeful.citymanager.audio.SoundPlayer
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.WindowsProgressBar
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun StartupScreen(
    onComplete: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN,
    autoStart: Boolean = false,
    soundPlayer: SoundPlayer? = null
) {
    var showProgressBar by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val progressAnimated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 3000,
            easing = LinearEasing
        ),
        label = "startup_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            if (!showProgressBar) {
                // Кнопка запуска
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Color(0xFF4A90E2),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            soundPlayer?.playStartupSound()
                            showProgressBar = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Запуск",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                VengText(
                    text = "Нажмите для запуска системы",
                    color = Color.White,
                    fontSize = 16.sp
                )
            } else {
                // Прогресс-бар
                VengText(
                    text = "Загрузка системы...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                WindowsProgressBar(
                    progress = progressAnimated,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(horizontal = 32.dp),
                    theme = theme
                )
            }
        }
    }

    LaunchedEffect(showProgressBar) {
        if (showProgressBar) {
            // Анимация прогресс-бара
            progress = 0f
            delay(100)
            progress = 0.2f
            delay(300)
            progress = 0.5f
            delay(500)
            progress = 0.8f
            delay(400)
            progress = 1f
            delay(700)
            onComplete()
        }
    }

    LaunchedEffect(autoStart) {
        if (autoStart) {
            soundPlayer?.playStartupSound()
            showProgressBar = true
        }
    }
}

