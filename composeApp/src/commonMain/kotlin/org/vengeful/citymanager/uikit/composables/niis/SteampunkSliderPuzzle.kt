package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun SteampunkSliderPuzzle(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    label: String = ""
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)
    val sliderCount = 3

    // Инициализируем слайдеры на основе текущего значения
    var sliders by remember(value) {
        // Распределяем значение между слайдерами
        val baseValue = value / sliderCount
        val remainder = value % sliderCount
        mutableStateOf(
            List(sliderCount) { index ->
                if (index < remainder) baseValue + 1 else baseValue
            }
        )
    }

    fun updateSliders(index: Int, newValue: Int) {
        val newSliders = sliders.toMutableList()
        val oldValue = newSliders[index]
        val delta = newValue - oldValue

        newSliders[index] = newValue.coerceIn(0, 100)

        // Влияние на соседние слайдеры (противоположное направление)
        if (index > 0) {
            val neighborValue = newSliders[index - 1] - delta / 2
            newSliders[index - 1] = neighborValue.coerceIn(0, 100)
        }
        if (index < sliderCount - 1) {
            val neighborValue = newSliders[index + 1] - delta / 2
            newSliders[index + 1] = neighborValue.coerceIn(0, 100)
        }

        sliders = newSliders

        // Вычисляем итоговое значение как среднее арифметическое
        val totalValue = sliders.sum()
        val calculatedValue = (totalValue / sliderCount.toFloat()).toInt().coerceIn(0, 100)
        onValueChange(calculatedValue)
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
                .fillMaxWidth()
                .height(200.dp)
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
                .drawBehind {
                    drawCircle(
                        color = colorScheme.rivets,
                        radius = 4f,
                        center = Offset(8f, 8f)
                    )
                    drawCircle(
                        color = colorScheme.rivets,
                        radius = 4f,
                        center = Offset(size.width - 8f, 8f)
                    )
                    drawCircle(
                        color = colorScheme.rivets,
                        radius = 4f,
                        center = Offset(8f, size.height - 8f)
                    )
                    drawCircle(
                        color = colorScheme.rivets,
                        radius = 4f,
                        center = Offset(size.width - 8f, size.height - 8f)
                    )
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                sliders.forEachIndexed { index, sliderValue ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "${index + 1}",
                            color = colorScheme.borderLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp)
                        )

                        Slider(
                            value = sliderValue.toFloat(),
                            onValueChange = { updateSliders(index, it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = colorScheme.borderLight,
                                activeTrackColor = colorScheme.borderLight,
                                inactiveTrackColor = colorScheme.borderDark.copy(alpha = 0.5f)
                            )
                        )

                        VengText(
                            text = "$sliderValue",
                            color = colorScheme.borderLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(35.dp)
                        )
                    }
                }
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


