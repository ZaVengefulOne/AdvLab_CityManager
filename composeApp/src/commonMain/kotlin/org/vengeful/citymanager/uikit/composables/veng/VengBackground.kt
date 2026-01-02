package org.vengeful.citymanager.uikit.composables.veng

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes

@Composable
fun VengBackground(
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundBrush = remember(theme) {
        SeveritepunkThemes.getBackground(theme)
    }

    val decorColor = remember(theme) {
        when (theme) {
            ColorTheme.GOLDEN -> Color(0x15D4AF37)
            ColorTheme.SEVERITE -> Color(0x154A90E2)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(backgroundBrush)
            .drawBehind {
                // Декоративные элементы в зависимости от темы
                drawCircle(
                    color = decorColor,
                    radius = 120f,
                    center = Offset(size.width - 80f, 80f)
                )
                drawCircle(
                    color = decorColor,
                    radius = 80f,
                    center = Offset(60f, size.height - 60f)
                )
            }
    ) {
        // Определяем, нужно ли использовать прокрутку на основе доступной высоты
        // Для экранов с низким разрешением (меньше 600dp по высоте) используем прокрутку
        val useScroll = maxHeight < 600.dp
        val scrollState = rememberScrollState()

        if (useScroll) {
            // Для маленьких экранов используем прокрутку
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                content()
            }
        } else {
            // Для больших экранов используем обычный Column без прокрутки
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                content()
            }
        }
    }
}