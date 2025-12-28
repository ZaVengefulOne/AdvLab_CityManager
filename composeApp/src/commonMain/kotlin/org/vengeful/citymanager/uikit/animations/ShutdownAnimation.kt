package org.vengeful.citymanager.uikit.animations

import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.app_name
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.BUILD_VERSION
import org.vengeful.citymanager.audio.SoundPlayer
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import kotlin.math.pow

enum class ShutdownPhase {
    INITIAL_ANIMATION,
    SOUND_PLAYING,
    CRT_EFFECT,
    BLACK_SCREEN
}

@Composable
fun ShutdownAnimation(
    onComplete: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN,
    soundPlayer: SoundPlayer? = null
) {
    var phase by remember { mutableStateOf(ShutdownPhase.INITIAL_ANIMATION) }
    var progress by remember { mutableStateOf(0f) }
    var crtProgress by remember { mutableStateOf(0f) }
    
    val transition = updateTransition(progress, label = "shutdown")

    val scale by transition.animateFloat(
        transitionSpec = { tween(2000) },
        label = "scale"
    ) { if (it > 0.5f) 0.8f else 1f }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(2000) },
        label = "alpha"
    ) { 1f - it }

    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }

    LaunchedEffect(Unit) {
        // Начальная анимация
        progress = 1f
        delay(2000)
        
        // Проигрываем звук выключения
        phase = ShutdownPhase.SOUND_PLAYING
        soundPlayer?.playShutdownSound()
        delay(500) // Даем время на начало звука
        
        // CRT-эффект
        phase = ShutdownPhase.CRT_EFFECT
        crtProgress = 0f
        delay(100) // Небольшая задержка перед началом
        crtProgress = 1f
        delay(800) // Длительность CRT-эффекта
        
        // Черный экран
        phase = ShutdownPhase.BLACK_SCREEN
        delay(300)
        
        onComplete()
    }

    when (phase) {
        ShutdownPhase.INITIAL_ANIMATION -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = progress * 0.9f))
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(colors.borderLight, CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        VengText(
                            text = "⏻",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }

                    VengText(
                        text = "Система отключается...",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.width(200.dp),
                        color = colors.borderLight
                    )

                    VengText(
                        text = stringResource(Res.string.app_name, BUILD_VERSION),
                        color = colors.borderLight.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
        
        ShutdownPhase.SOUND_PLAYING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Показываем черный экран во время звука
            }
        }
        
        ShutdownPhase.CRT_EFFECT -> {
            CRTEffect(
                progress = crtProgress,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        ShutdownPhase.BLACK_SCREEN -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}

@Composable
private fun CRTEffect(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "crt_progress"
    )
    
    Box(
        modifier = modifier.background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = kotlin.math.sqrt(
                (size.width / 2).pow(2) + (size.height / 2).pow(2)
            )
            
            // Сначала весь экран белый, затем затемнение к центру
            if (animatedProgress < 0.1f) {
                // Белый экран
                drawRect(
                    color = Color.White,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height)
                )
            } else {
                // Затемнение к центру
                val fadeProgress = ((animatedProgress - 0.1f) / 0.9f).coerceIn(0f, 1f)
                val currentRadius = maxRadius * (1f - fadeProgress)
                
                // Рисуем градиент от белого к черному
                val gradient = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 1f - fadeProgress),
                        Color.Black
                    ),
                    center = Offset(centerX, centerY),
                    radius = currentRadius.coerceAtLeast(1f)
                )
                
                drawRect(
                    brush = gradient,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height)
                )
            }
        }
    }
}
