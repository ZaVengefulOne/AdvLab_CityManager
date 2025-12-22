package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.screens.niis.DialLockHints
import kotlin.math.*
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

data class DialLockHints(
    val sumOfDigits: Int,
    val productOfFirstTwo: Int,
    val maxMinDifference: Int
)

@Composable
fun SteampunkDialLock(
    value: Int,
    onValueChange: (Int) -> Unit,
    hints: DialLockHints? = null,
    targetValue: Int? = null,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)

    // Разбиваем значение на сотни, десятки и единицы
    val hundreds = (value / 100) % 10
    val tens = (value / 10) % 10
    val ones = value % 10

    // Вычисляем близость к целевому значению (0-100%)
    val proximity = if (targetValue != null && targetValue in 0..999 && value in 0..999) {
        val difference = abs(value - targetValue)
        // Максимальная возможная разница в диапазоне 0-999
        val maxDifference = 999f
        // Близость: 100% при разнице 0, 0% при максимальной разнице
        (100f - (difference / maxDifference * 100f)).coerceIn(0f, 100f)
    } else null

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

        // Подсказки
//        if (hints != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        colorScheme.background.copy(alpha = 0.5f),
//                        RoundedCornerShape(8.dp)
//                    )
//                    .border(
//                        width = 1.dp,
//                        color = colorScheme.borderDark.copy(alpha = 0.5f),
//                        shape = RoundedCornerShape(8.dp)
//                    )
//                    .padding(8.dp)
//            ) {
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    VengText(
//                        text = "Сумма цифр: ${hints.sumOfDigits}",
//                        color = colorScheme.borderLight,
//                        fontSize = 11.sp
//                    )
//                    VengText(
//                        text = "Произведение сотен×десятков: ${hints.productOfFirstTwo}",
//                        color = colorScheme.borderLight,
//                        fontSize = 11.sp
//                    )
//                    VengText(
//                        text = "Разность макс-мин: ${hints.maxMinDifference}",
//                        color = colorScheme.borderLight,
//                        fontSize = 11.sp
//                    )
//                }
//            }
//        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    colorScheme.background.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.borderLight, colorScheme.borderDark)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Диск сотен
                DialComponent(
                    digit = hundreds,
                    label = "Сотни",
                    getCurrentValue = { value },
                    dialType = 0,
                    onValueChange = onValueChange,
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f)
                )

                // Диск десятков
                DialComponent(
                    digit = tens,
                    label = "Десятки",
                    getCurrentValue = { value },
                    dialType = 1,
                    onValueChange = onValueChange,
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f)
                )

                // Диск единиц
                DialComponent(
                    digit = ones,
                    label = "Единицы",
                    getCurrentValue = { value },
                    dialType = 2,
                    onValueChange = onValueChange,
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Индикатор близости
        if (proximity != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Близость:",
                    color = colorScheme.text,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            colorScheme.background.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(proximity / 100f)
                            .background(
                                when {
                                    proximity > 80f -> Color(0xFF4CAF50) // Зеленый
                                    proximity > 50f -> Color(0xFFFFC107) // Желтый
                                    proximity > 25f -> Color(0xFFFF9800) // Оранжевый
                                    else -> Color(0xFFF44336) // Красный
                                },
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                VengText(
                    text = "${proximity.toInt()}%",
                    color = colorScheme.borderLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        VengText(
            text = "$value",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DialComponent(
    digit: Int,
    label: String,
    getCurrentValue: () -> Int,
    dialType: Int,
    onValueChange: (Int) -> Unit,
    colorScheme: org.vengeful.citymanager.uikit.SeveritepunkColorScheme,
    modifier: Modifier = Modifier
) {
    // Угол для отображения стрелки (как в SteampunkDial)
    val angle = (digit / 10f) * 360f - 90f

    // Храним актуальное значение в remember state
    val currentValueState = remember { mutableStateOf(getCurrentValue()) }

    // Обновляем state при изменении значения
    LaunchedEffect(getCurrentValue()) {
        currentValueState.value = getCurrentValue()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        VengText(
            text = label,
            color = colorScheme.text,
            fontSize = 11.sp
        )

        Box(
            modifier = Modifier
                .size(80.dp)
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
                .pointerInput(Unit) {
                    // Используем точно такую же логику, как в SteampunkDial
                    detectDragGestures { change, _ ->
                        val center = size.width / 2f
                        val offset = change.position - Offset(center, center)
                        val angleRad = atan2(offset.y, offset.x) + PI / 2
                        val normalizedAngle = (angleRad / (2 * PI) + 1f) % 1f
                        val newDigit = (normalizedAngle * 10).toInt().coerceIn(0, 9)

                        // Получаем актуальное значение из state
                        val currentValue = currentValueState.value
                        val currentHundreds = (currentValue / 100) % 10
                        val currentTens = (currentValue / 10) % 10
                        val currentOnes = currentValue % 10

                        // Вычисляем новое значение в зависимости от типа диска
                        val newValue = when (dialType) {
                            0 -> newDigit * 100 + currentTens * 10 + currentOnes // Сотни
                            1 -> currentHundreds * 100 + newDigit * 10 + currentOnes // Десятки
                            else -> currentHundreds * 100 + currentTens * 10 + newDigit // Единицы
                        }.coerceIn(0, 999)

                        onValueChange(newValue)
                        // Обновляем state сразу после изменения
                        currentValueState.value = newValue
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 12

                // Рисуем деления (как в SteampunkDial, но для 10 значений)
                for (i in 0..9) {
                    val angleRad = Math.toRadians((i / 10f * 360f - 90).toDouble())
                    val isCurrent = i == digit
                    val lineLength = if (isCurrent) 10f else 5f
                    val lineStart = radius - lineLength

                    val startX = center.x + cos(angleRad) * lineStart
                    val startY = center.y + sin(angleRad) * lineStart
                    val endX = center.x + cos(angleRad) * radius
                    val endY = center.y + sin(angleRad) * radius

                    drawLine(
                        color = if (isCurrent) colorScheme.borderLight else colorScheme.borderDark,
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = if (isCurrent) 2.5f else 1.5f
                    )
                }

                // Рисуем стрелку (как в SteampunkDial)
                val angleRad = Math.toRadians(angle.toDouble())
                val arrowLength = radius - 8
                val arrowEndX = center.x + cos(angleRad) * arrowLength
                val arrowEndY = center.y + sin(angleRad) * arrowLength

                drawLine(
                    color = colorScheme.borderLight,
                    start = center,
                    end = Offset(arrowEndX.toFloat(), arrowEndY.toFloat()),
                    strokeWidth = 3f
                )
            }

            VengText(
                text = "$digit",
                color = colorScheme.borderLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
