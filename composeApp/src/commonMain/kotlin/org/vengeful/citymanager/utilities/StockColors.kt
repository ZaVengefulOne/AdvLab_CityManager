package org.vengeful.citymanager.utilities

import androidx.compose.ui.graphics.Color

object StockColors {
    val colors = listOf(
        Color(0xFF4A90E2),  // Синий
        Color(0xFF27AE60),  // Зеленый
        Color(0xFFE74C3C),  // Красный
        Color(0xFFF39C12),  // Оранжевый
        Color(0xFF9B59B6),  // Фиолетовый
        Color(0xFF1ABC9C),  // Бирюзовый
        Color(0xFFE67E22)   // Темно-оранжевый
    )

    fun getColorForIndex(index: Int): Color {
        return colors[index % colors.size]
    }
}
