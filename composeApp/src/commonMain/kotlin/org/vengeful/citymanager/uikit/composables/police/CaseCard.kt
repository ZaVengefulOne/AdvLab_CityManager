package org.vengeful.citymanager.uikit.composables.police

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun CaseCard(
    case: Case,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colors = SeveritepunkThemes.getColorScheme(theme)

    val statusText = when (case.status) {
        CaseStatus.OPEN -> "Открыто"
        CaseStatus.SENT_TO_COURT -> "Передано в суд"
        CaseStatus.VERDICT_PRONOUNCED -> "Вынесен приговор"
        CaseStatus.CLOSED -> "Закрыто"
    }

    val statusColor = when (case.status) {
        CaseStatus.OPEN -> Color(0xFF4CAF50)
        CaseStatus.SENT_TO_COURT -> Color(0xFFFF9800)
        CaseStatus.VERDICT_PRONOUNCED -> Color(0xFF2196F3)
        CaseStatus.CLOSED -> Color(0xFF9E9E9E)
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Дело №${case.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    VengText(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            VengText(
                text = "Заявитель: ${case.complainantName}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            VengText(
                text = "Подозреваемый: ${case.suspectName}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            VengText(
                text = "Статья: ${case.violationArticle}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (case.statementText.isNotBlank()) {
                val previewText = if (case.statementText.length > 100) {
                    case.statementText.take(100) + "..."
                } else {
                    case.statementText
                }
                VengText(
                    text = "Заявление: $previewText",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

