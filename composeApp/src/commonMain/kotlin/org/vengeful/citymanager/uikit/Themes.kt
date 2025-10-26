package org.vengeful.citymanager.uikit

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Enum для доступных тем
enum class ColorTheme {
    GOLDEN,   // Классический золотой стимпанк
    SEVERITE  // Северный синий северитпанк
}

// Основной класс для управления темами
object SeveritepunkThemes {
    fun getColorScheme(theme: ColorTheme): SeveritepunkColorScheme {
        return when (theme) {
            ColorTheme.GOLDEN -> goldenScheme()
            ColorTheme.SEVERITE -> severiteScheme()
        }
    }

    fun getCardColors(theme: ColorTheme): SeveritepunkCardColors {
        return when (theme) {
            ColorTheme.GOLDEN -> goldenCardScheme()
            ColorTheme.SEVERITE -> severiteCardScheme()
        }
    }

    fun getTextFieldColors(theme: ColorTheme): SeveritepunkTextFieldColors {
        return when (theme) {
            ColorTheme.GOLDEN -> goldenTextFieldScheme()
            ColorTheme.SEVERITE -> severiteTextFieldScheme()
        }
    }

    fun getBackground(theme: ColorTheme): Brush {
        return when (theme) {
            ColorTheme.GOLDEN -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF3A2C1A),
                    Color(0xFF5D4A2E),
                    Color(0xFF8B7355)
                )
            )

            ColorTheme.SEVERITE -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C3E50),
                    Color(0xFF34495E),
                    Color(0xFF4A6572)
                )
            )
        }
    }
}

// Золотая цветовая схема (классический стимпанк)
private fun goldenScheme() = SeveritepunkColorScheme(
    background = Color(0xFF8B7355),
    borderLight = Color(0xFFD4AF37),
    borderDark = Color(0xFF5D4037),
    text = Color(0xFF2C1810),
    rivets = Color(0xFFC0C0C0)
)

private fun goldenCardScheme() = SeveritepunkCardColors(
    background = Color(0xFF4A3C2A),
    borderLight = Color(0xFFD4AF37),
    borderDark = Color(0xFF8B7355),
    text = Color(0xFFE8D9B5),
    accent = Color(0xFFD4AF37),
    rivets = Color(0xFFC0C0C0)
)

private fun goldenTextFieldScheme() = SeveritepunkTextFieldColors(
    background = Color(0xFF3A2C1A),
    borderLight = Color(0xFFD4AF37),
    borderDark = Color(0xFF8B7355),
    text = Color(0xFFE8D9B5),
    label = Color(0xFFD4AF37),
    placeholder = Color(0xFF8B7355)
)

// Северитовая цветовая схема
private fun severiteScheme() = SeveritepunkColorScheme(
    background = Color(0xFF3A506B),
    borderLight = Color(0xFF4A90E2),
    borderDark = Color(0xFF2C3E50),
    text = Color(0xFFE8F1F5),
    rivets = Color(0xFFA8D0E6)
)

private fun severiteCardScheme() = SeveritepunkCardColors(
    background = Color(0xFF34495E),
    borderLight = Color(0xFF4A90E2),
    borderDark = Color(0xFF2C3E50),
    text = Color(0xFFE8F1F5),
    accent = Color(0xFF4A90E2),
    rivets = Color(0xFFA8D0E6)
)

private fun severiteTextFieldScheme() = SeveritepunkTextFieldColors(
    background = Color(0xFF2C3E50),
    borderLight = Color(0xFF4A90E2),
    borderDark = Color(0xFF34495E),
    text = Color(0xFFE8F1F5),
    label = Color(0xFF4A90E2),
    placeholder = Color(0xFF7B9EB0)
)

data class SeveritepunkColorScheme(
    val background: Color,
    val borderLight: Color,
    val borderDark: Color,
    val text: Color,
    val rivets: Color
)

data class SeveritepunkTextFieldColors(
    val background: Color,
    val borderLight: Color,
    val borderDark: Color,
    val text: Color,
    val label: Color,
    val placeholder: Color
)

data class SeveritepunkCardColors(
    val background: Color,
    val borderLight: Color,
    val borderDark: Color,
    val text: Color,
    val accent: Color,
    val rivets: Color
)

data class DialogColors(
    val background: Color,
    val borderLight: Color,
    val borderDark: Color,
    val surface: Color
)

// Функция для затемнения цвета
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Функция для осветления цвета
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceIn(0f, 1f),
        green = (green + (1 - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}