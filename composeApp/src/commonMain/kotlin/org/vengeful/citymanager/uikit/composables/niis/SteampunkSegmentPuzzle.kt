package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun SteampunkSegmentPuzzle(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val segmentCount = 8
    val segmentAngle = 360f / segmentCount

    var rotations by remember {
        // Инициализируем вращения на основе значения
        // Если value = 0, все сегменты не выровнены (случайные углы, не кратные 45)
        // Если value > 0, вычисляем базовое вращение
        mutableStateOf(if (value == 0) {
            // Для значения 0: все сегменты не выровнены (углы не кратные 45)
            List(segmentCount) { index ->
                // Генерируем угол, который не кратен 45 (не выровнен)
                // Используем смещение от 10 до 35 градусов от базового угла
                val baseAngle = index * 45f
                val offset = 10f + (index * 7f) % 25f // Смещение от 10 до 35 градусов
                (baseAngle + offset) % 360f
            }
        } else {
            val baseRotation = (value / 1000f) * 360f
            List(segmentCount) { index ->
                (baseRotation + index * 45f) % 360f
            }
        })
    }

    // Вычисляем значение на основе выравнивания сегментов
    val alignedSegments = rotations.count { rotation ->
        val normalized = rotation % 45f
        normalized < 5f || normalized > 40f
    }
    val calculatedValue = ((alignedSegments.toFloat() / segmentCount) * 1000f).toInt().coerceIn(0, 1000)

    // Синхронизируем значение с родителем
    if (calculatedValue != value) {
        onValueChange(calculatedValue)
    }

    fun rotateSegment(index: Int, deltaAngle: Float) {
        val newRotations = rotations.toMutableList()
        newRotations[index] = (newRotations[index] + deltaAngle + 360f) % 360f
        rotations = newRotations
    }

    // Храним актуальные значения в remember state для плавного вращения
    val rotationsState = remember { mutableStateOf(rotations) }

    // Обновляем state при изменении rotations
    LaunchedEffect(rotations) {
        rotationsState.value = rotations
    }

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
                .size(180.dp)
                .background(
                    colorScheme.background.copy(alpha = 0.7f),
                    CircleShape
                )
                .border(
                    width = 3.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = CircleShape
                )
                .shadow(6.dp, CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        var lastAngle = 0f
                        var currentSegmentIndex = -1

                        detectDragGestures(
                            onDragStart = { offset ->
                                val center = size.width / 2f
                                val offsetFromCenter = offset - Offset(center, center)
                                val distance = sqrt(offsetFromCenter.x * offsetFromCenter.x + offsetFromCenter.y * offsetFromCenter.y)
                                val radius = minOf(size.width, size.height) / 2 - 20

                                // Проверяем, что клик внутри круга
                                if (distance <= radius) {
                                    val clickAngle = (atan2(offsetFromCenter.y, offsetFromCenter.x) * 180f / PI.toFloat() + 360f) % 360f

                                    // Получаем актуальные значения из state
                                    val currentRotations = rotationsState.value

                                    // Определяем, в какой сегмент попал клик, учитывая текущие вращения
                                    for (i in 0 until segmentCount) {
                                        val baseStartAngle = i * segmentAngle
                                        val segmentRotation = currentRotations[i]

                                        // Вычисляем реальные углы сегмента с учётом вращения
                                        val relativeAngle = (clickAngle - segmentRotation + 360f) % 360f

                                        // Проверяем, попадает ли относительный угол в базовый диапазон сегмента
                                        if (relativeAngle >= baseStartAngle && relativeAngle < baseStartAngle + segmentAngle) {
                                            currentSegmentIndex = i
                                            lastAngle = clickAngle
                                            break
                                        }
                                    }

                                    if (currentSegmentIndex == -1) {
                                        // Если не попали ни в один сегмент, используем базовую логику
                                        currentSegmentIndex = ((clickAngle / segmentAngle).toInt()) % segmentCount
                                        lastAngle = clickAngle
                                    }
                                } else {
                                    currentSegmentIndex = -1
                                }
                            }
                        ) { change, _ ->
                            if (currentSegmentIndex >= 0) {
                                val center = size.width / 2f
                                val offsetFromCenter = change.position - Offset(center, center)
                                val currentAngle = (atan2(offsetFromCenter.y, offsetFromCenter.x) * 180f / PI.toFloat() + 360f) % 360f

                                // Вычисляем изменение угла напрямую от начальной позиции
                                var deltaAngle = currentAngle - lastAngle

                                // Нормализуем угол
                                if (deltaAngle > 180f) deltaAngle -= 360f
                                if (deltaAngle < -180f) deltaAngle += 360f

                                // Вращаем сегмент на вычисленную дельту
                                rotateSegment(currentSegmentIndex, deltaAngle)

                                // Обновляем state сразу после изменения
                                rotationsState.value = rotations

                                // Обновляем lastAngle для следующего кадра
                                lastAngle = currentAngle
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 20

                // Рисуем сегменты
                for (i in 0 until segmentCount) {
                    val baseStartAngle = i * segmentAngle
                    val segmentRotation = rotations[i]

                    // Вычисляем реальные углы сегмента с учётом вращения
                    val startAngle = (baseStartAngle + segmentRotation) * PI.toFloat() / 180f
                    val endAngle = (baseStartAngle + segmentAngle + segmentRotation) * PI.toFloat() / 180f

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(
                            center.x + cos(startAngle) * radius,
                            center.y + sin(startAngle) * radius
                        )
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                center.x - radius,
                                center.y - radius,
                                center.x + radius,
                                center.y + radius
                            ),
                            startAngleDegrees = startAngle * 180f / PI.toFloat(),
                            sweepAngleDegrees = segmentAngle,
                            forceMoveTo = false
                        )
                        lineTo(center.x, center.y)
                        close()
                    }

                    val isAligned = (segmentRotation % 45f) < 5f || (segmentRotation % 45f) > 40f
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isAligned) colorScheme.borderLight else colorScheme.borderDark,
                                if (isAligned) colorScheme.borderLight.copy(alpha = 0.3f) else colorScheme.borderDark.copy(alpha = 0.2f)
                            ),
                            center = center,
                            radius = radius
                        )
                    )

                    // Рисуем границы сегментов
                    val borderStartX = center.x + cos(startAngle) * (radius * 0.7f)
                    val borderStartY = center.y + sin(startAngle) * (radius * 0.7f)
                    val borderEndX = center.x + cos(startAngle) * radius
                    val borderEndY = center.y + sin(startAngle) * radius

                    drawLine(
                        color = colorScheme.borderDark,
                        start = Offset(borderStartX, borderStartY),
                        end = Offset(borderEndX, borderEndY),
                        strokeWidth = 2f
                    )
                }

                // Рисуем центральный круг
                drawCircle(
                    color = colorScheme.borderLight.copy(alpha = 0.5f),
                    radius = 15f,
                    center = center
                )

                // Рисуем деления на внешнем краю
                for (i in 0 until segmentCount) {
                    val angleRad = (i * segmentAngle) * PI.toFloat() / 180f
                    val startX = center.x + cos(angleRad) * radius
                    val startY = center.y + sin(angleRad) * radius
                    val endX = center.x + cos(angleRad) * (radius + 8f)
                    val endY = center.y + sin(angleRad) * (radius + 8f)

                    drawLine(
                        color = colorScheme.borderLight,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
            }
        }

        VengText(
            text = "$calculatedValue",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
