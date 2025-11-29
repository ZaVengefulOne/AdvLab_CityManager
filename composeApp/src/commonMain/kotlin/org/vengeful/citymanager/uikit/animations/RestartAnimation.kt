package org.vengeful.citymanager.uikit.animations

import androidx.compose.animation.core.*
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import org.vengeful.citymanager.uikit.composables.veng.VengText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RestartAnimation(
    onComplete: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var phase by remember { mutableStateOf(0) }
    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }

    val rotation by animateFloatAsState(
        targetValue = when (phase) {
            1 -> 180f
            2 -> 360f
            else -> 0f
        },
        animationSpec = tween(1000),
        label = "restart_rotation"
    )

    LaunchedEffect(Unit) {
        phase = 1
        delay(1000)
        phase = 2
        delay(1000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                // ИСПРАВЛЕННАЯ шестерёнка с зубцами по всем сторонам
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 3

                    // Основной круг шестерёнки
                    drawCircle(
                        color = colors.borderLight,
                        center = center,
                        radius = radius
                    )

                    // Зубцы шестерёнки - 12 зубцов равномерно распределены
                    val toothCount = 12
                    val toothLength = radius * 0.4f
                    val toothWidth = radius * 0.3f

                    for (i in 0 until toothCount) {
                        val angle = i * (360f / toothCount)
                        val radian = Math.toRadians(angle.toDouble())

                        // Позиция зубца
                        val toothCenterX = center.x + cos(radian).toFloat() * (radius + toothLength / 2)
                        val toothCenterY = center.y + sin(radian).toFloat() * (radius + toothLength / 2)

                        // Рисуем зубцы как прямоугольники, повёрнутые в нужном направлении
                        drawRect(
                            color = colors.borderLight,
                            topLeft = Offset(
                                x = toothCenterX - toothWidth / 2,
                                y = toothCenterY - toothLength / 2
                            ),
                            size = Size(toothWidth, toothLength)
                        )
                    }

                    // Центральное отверстие
                    drawCircle(
                        color = colors.background,
                        center = center,
                        radius = radius * 0.3f
                    )
                }
            }

            VengText(
                text = "Перезагрузка Системы",
                color = colors.borderLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Индикатор прогресса
            LinearProgressIndicator(
                progress = { (phase + rotation / 360f) / 3f },
                modifier = Modifier.width(200.dp),
                color = colors.borderLight
            )
        }
    }
}
