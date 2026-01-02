package org.vengeful.citymanager.uikit.composables.court

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun HearingCard(
    hearing: Hearing,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    val colors = SeveritepunkThemes.getColorScheme(theme)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.borderLight, colors.background)
                    )
                )
                .border(2.dp, colors.borderLight, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            VengText(
                text = "Слушание №${hearing.id}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            VengText(
                text = "Дело №${hearing.caseId}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            VengText(
                text = "Истец: ${hearing.plaintiffName}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (hearing.verdict.isNotBlank()) {
                val previewText = if (hearing.verdict.length > 100) {
                    hearing.verdict.take(100) + "..."
                } else {
                    hearing.verdict
                }
                VengText(
                    text = "Вердикт: $previewText",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (hearing.protocol.isNotBlank()) {
                val previewText = if (hearing.protocol.length > 100) {
                    hearing.protocol.take(100) + "..."
                } else {
                    hearing.protocol
                }
                VengText(
                    text = "Протокол: $previewText",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}



