package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import androidx.compose.foundation.layout.Box
import kotlin.math.abs

data class SwitchPuzzleHints(
    val activeSwitchesCount: Int,
    val totalSwitches: Int
)

@Composable
fun SteampunkSwitchPuzzle(
    value: Int,
    onValueChange: (Int) -> Unit,
    targetValue: Int? = null,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val gridSize = 5
    val totalSwitches = gridSize * gridSize

    // Инициализируем состояние переключателей на основе текущего значения
    var switches by remember(value) {
        val targetCount = ((value / 100f) * totalSwitches).toInt().coerceIn(0, totalSwitches)
        mutableStateOf(List(totalSwitches) { index -> index < targetCount })
    }

    // Обновляем значение при изменении переключателей
    val enabledCount = switches.count { it }
    val calculatedValue = ((enabledCount.toFloat() / totalSwitches) * 100f).toInt().coerceIn(0, 100)

    // Вычисляем близость к целевому значению (0-100%)
    val proximity = if (targetValue != null && targetValue in 0..100 && calculatedValue in 0..100) {
        val difference = abs(calculatedValue - targetValue)
        val maxDifference = 100f
        (100f - (difference / maxDifference * 100f)).coerceIn(0f, 100f)
    } else null

    // Синхронизируем значение с родителем
    if (calculatedValue != value) {
        onValueChange(calculatedValue)
    }

    fun toggleSwitch(row: Int, col: Int) {
        val newSwitches = switches.toMutableList()
        val index = row * gridSize + col

        // Переключаем сам элемент
        newSwitches[index] = !newSwitches[index]

        // Переключаем соседей (вверх, вниз, влево, вправо)
        if (row > 0) {
            val topIndex = (row - 1) * gridSize + col
            newSwitches[topIndex] = !newSwitches[topIndex]
        }
        if (row < gridSize - 1) {
            val bottomIndex = (row + 1) * gridSize + col
            newSwitches[bottomIndex] = !newSwitches[bottomIndex]
        }
        if (col > 0) {
            val leftIndex = row * gridSize + (col - 1)
            newSwitches[leftIndex] = !newSwitches[leftIndex]
        }
        if (col < gridSize - 1) {
            val rightIndex = row * gridSize + (col + 1)
            newSwitches[rightIndex] = !newSwitches[rightIndex]
        }

        switches = newSwitches
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
                .size(200.dp)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(gridSize) { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(gridSize) { col ->
                            val index = row * gridSize + col
                            val isEnabled = switches[index]

                            val switchPosition by animateFloatAsState(
                                targetValue = if (isEnabled) 1f else 0f,
                                animationSpec = tween(200),
                                label = "switch_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isEnabled) {
                                            colorScheme.borderLight.copy(alpha = 0.3f)
                                        } else {
                                            colorScheme.background.copy(alpha = 0.5f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                colorScheme.borderLight.copy(alpha = if (isEnabled) 1f else 0.5f),
                                                colorScheme.borderDark.copy(alpha = if (isEnabled) 0.8f else 0.5f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .shadow(
                                        elevation = if (isEnabled) 3.dp else 1.dp,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        toggleSwitch(row, col)
                                    }
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((switchPosition * 22f + 4f).dp)
                                        .background(
                                            if (isEnabled) {
                                                colorScheme.borderLight
                                            } else {
                                                colorScheme.borderDark.copy(alpha = 0.3f)
                                            },
                                            CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isEnabled) {
                                                colorScheme.borderDark
                                            } else {
                                                colorScheme.borderLight.copy(alpha = 0.3f)
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
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
            text = "$calculatedValue",
            color = colorScheme.borderLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
