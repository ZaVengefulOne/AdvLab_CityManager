package org.vengeful.citymanager.uikit.composables.court

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AppealHearingDialog(
    hearing: Hearing,
    onDismiss: () -> Unit,
    onUpdateHearing: (Hearing) -> Unit,
    onUpdateCaseStatus: (Int, CaseStatus) -> Unit,
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    var protocol by remember { mutableStateOf("") }
    var verdict by remember { mutableStateOf("") }

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
                        text = "Апелляция по слушанию №${hearing.id}",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    VengText(
                        text = "Дело №${hearing.caseId}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Протокол (large field, cleared)
                    VengTextField(
                        value = protocol,
                        onValueChange = { protocol = it },
                        label = "Протокол",
                        placeholder = "Введите текст протокола слушания...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        theme = theme,
                        maxLines = 15
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Вердикт (small field, cleared)
                    VengTextField(
                        value = verdict,
                        onValueChange = { verdict = it },
                        label = "Вердикт",
                        placeholder = "Введите решение суда...",
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
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
                            if (protocol.isNotBlank() && verdict.isNotBlank()) {
                                // Update hearing with new protocol and verdict
                                val updatedHearing = hearing.copy(
                                    protocol = protocol,
                                    verdict = verdict,
                                    updatedAt = Clock.System.now().toEpochMilliseconds()
                                )
                                onUpdateHearing(updatedHearing)
                                // Set case status to VERDICT_PRONOUNCED after saving
                                onUpdateCaseStatus(hearing.caseId, CaseStatus.VERDICT_PRONOUNCED)
                                onDismiss()
                            }
                        },
                        text = "Сохранить",
                        enabled = protocol.isNotBlank() && verdict.isNotBlank(),
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

