package org.vengeful.citymanager.uikit.composables.stocks


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Suppress("DefaultLocale")
@Composable
fun StockGraph(
    stockName: String,
    currentPrice: Double,
    history: List<Double>,
    modifier: Modifier = Modifier,
    graphColor: Color = Color(0xFF4A90E2),
    backgroundColor: Color = Color(0xFF2C3E50),
    gridColor: Color = Color(0xFF3A4A5A)
) {
    Column(
        modifier = modifier
            .background(backgroundColor)
            .border(1.dp, graphColor.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок
        VengText(
            text = stockName.uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // График
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val padding = 20f
                val graphWidth = width - padding * 2
                val graphHeight = height - padding * 2

                // Рисуем сетку
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = padding + (graphHeight / gridLines) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }

                for (i in 0..10) {
                    val x = padding + (graphWidth / 10) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(x, padding),
                        end = Offset(x, height - padding),
                        strokeWidth = 1f
                    )
                }

                // Рисуем график
                if (history.isNotEmpty()) {
                    val minValue = history.minOrNull() ?: 0.0
                    val maxValue = history.maxOrNull() ?: 100.0
                    val range = (maxValue - minValue).coerceAtLeast(10.0)

                    val path = Path()
                    history.forEachIndexed { index, value ->
                        val x = padding + (graphWidth / (history.size - 1).coerceAtLeast(1)) * index
                        val normalizedValue = ((value - minValue) / range).coerceIn(0.0, 1.0)
                        val y = padding + graphHeight - (normalizedValue * graphHeight)

                        if (index == 0) {
                            path.moveTo(x, y.toFloat())
                        } else {
                            path.lineTo(x, y.toFloat())
                        }
                    }

                    drawPath(
                        path = path,
                        color = graphColor,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // Нижняя панель с ценой
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(graphColor.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                VengText(
                    text = "ЦЕНА",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            VengText(
                text = String.format("%.2f", currentPrice),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
