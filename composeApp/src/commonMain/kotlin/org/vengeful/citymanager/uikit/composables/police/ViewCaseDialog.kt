package org.vengeful.citymanager.uikit.composables.police

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun ViewCaseDialog(
    case: Case,
    onDismiss: () -> Unit,
    onDelete: (Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val dialogColors = remember(theme) {
        when (theme) {
            ColorTheme.GOLDEN -> DialogColors(
                background = Color(0xFF4A3C2A),
                borderLight = Color(0xFFD4AF37),
                borderDark = Color(0xFF8B7355),
                surface = Color(0xFF5D4A2E)
            )
            ColorTheme.SEVERITE -> DialogColors(
                background = Color(0xFF34495E),
                borderLight = Color(0xFF4A90E2),
                borderDark = Color(0xFF2C3E50),
                surface = Color(0xFF2C3E50)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(dialogColors.borderLight, dialogColors.borderDark)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(8.dp, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(dialogColors.surface, dialogColors.background)
                        )
                    )
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = "Дело №${case.id} (Архив)",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Следователь
                    VengText(
                        text = "Следователь: ID ${case.investigatorPersonId}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Заявитель
                    VengText(
                        text = "Заявитель: ${case.complainantName}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Подозреваемый
                    VengText(
                        text = "Подозреваемый: ${case.suspectName}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Статья правонарушения
                    VengText(
                        text = "Статья правонарушения: ${case.violationArticle}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Текст заявления
                    VengText(
                        text = "Текст заявления:",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VengText(
                        text = case.statementText,
                        color = dialogColors.borderLight.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Статус
                    VengText(
                        text = "Статус: Закрыто",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Отмена",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            onDelete(case.id)
                            onDismiss()
                        },
                        text = "Удалить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}

