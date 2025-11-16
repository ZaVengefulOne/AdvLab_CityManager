package org.vengeful.citymanager.uikit.composables.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun AccessIndicator(
    hasAccess: Boolean,
    theme: ColorTheme = ColorTheme.GOLDEN,
    modifier: Modifier = Modifier
) {
    val colorScheme = SeveritepunkThemes.getColorScheme(theme)

    // Цвета для индикатора: зеленый для доступа, красный для отсутствия доступа
    val indicatorColor = if (hasAccess) {
        Color(0xFF4CAF50) // Зеленый
    } else {
        Color(0xFFF44336) // Красный
    }

    // Цвета для границы в стиле steampunk
    val borderLight = if (hasAccess) {
        Color(0xFF81C784) // Светло-зеленый
    } else {
        Color(0xFFE57373) // Светло-красный
    }

    val borderDark = if (hasAccess) {
        Color(0xFF2E7D32) // Темно-зеленый
    } else {
        Color(0xFFC62828) // Темно-красный
    }

    Surface(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(indicatorColor)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderLight, borderDark)
                ),
                shape = CircleShape
            )
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = if (hasAccess) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFF44336).copy(alpha = 0.5f)
            ),
        shape = CircleShape,
        color = indicatorColor
    ) {
        // Внутренний круг для эффекта глубины
        Surface(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = if (hasAccess) {
                Color(0xFFA5D6A7) // Светло-зеленый центр
            } else {
                Color(0xFFEF9A9A) // Светло-красный центр
            }
        ) {}
    }
}