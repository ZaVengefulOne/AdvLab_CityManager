package org.vengeful.citymanager.uikit.animations
import androidx.compose.animation.core.*
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShutdownAnimation(
    onComplete: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var progress by remember { mutableStateOf(0f) }
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
        progress = 1f
        delay(2000)
        onComplete() // Закрываем приложение
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = progress * 0.9f))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
//                alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Стилизованная иконка выключения
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(colors.borderLight, CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏻",
                    fontSize = 32.sp,
                    color = Color.White
                )
            }

            Text(
                text = "СИСТЕМА ОТКЛЮЧАЕТСЯ",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Анимированный индикатор прогресса
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.width(200.dp),
                color = colors.borderLight
            )

            Text(
                text = "Терминал Системы Городского Управления v0.0.1",
                color = colors.borderLight.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}