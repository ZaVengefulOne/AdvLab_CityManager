package org.vengeful.citymanager.uikit.composables.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun ThemeSwitcher(
    currentTheme: ColorTheme,
    onThemeChange: (ColorTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val currentScheme = SeveritepunkThemes.getColorScheme(currentTheme)

    Box(modifier = modifier) {
        VengButton(
            onClick = { expanded = true },
            text = when (currentTheme) {
                ColorTheme.GOLDEN -> "🎩 Тема"
                ColorTheme.SEVERITE -> "🏔️ Тема"
            },
            theme = currentTheme,
            modifier = modifier.fillMaxWidth(),
            padding = 12.dp
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(currentScheme.background)
                .border(2.dp, currentScheme.borderLight, RoundedCornerShape(6.dp))
        ) {
            ColorTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    onClick = {
                        onThemeChange(theme)
                        expanded = false
                    },
                    modifier = Modifier.background(currentScheme.background),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Индикатор выбранной темы
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(2.dp, currentScheme.borderLight, CircleShape)
                                    .background(
                                        if (theme == currentTheme) currentScheme.borderLight else Color.Transparent,
                                        CircleShape
                                    )
                            )

                            VengText(
                                text = when (theme) {
                                    ColorTheme.GOLDEN -> "🎩 Золотая"
                                    ColorTheme.SEVERITE -> "🏔️ Северная"
                                },
                                color = currentScheme.text,
                                fontSize = 14.sp,
                                fontWeight = if (theme == currentTheme) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
    }
}
