package org.vengeful.citymanager.uikit.composables.person

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
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun PersonDossierDialog(
    person: Person,
    casesAsSuspect: List<Case> = emptyList(),
    bankBalance: Double? = null,
    hasBankAccount: Boolean = false,
    onDismiss: () -> Unit,
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

    // Определяем место работы
    val workplace = remember(person.rights) {
        val firstRight = person.rights.firstOrNull()
        when {
            firstRight == null -> "неизвестно"
            firstRight == Rights.Any -> "неизвестно"
            else -> firstRight.getDisplayName()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
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
                        text = "Досье жителя",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // ФИО
                    InfoRow(
                        label = "ФИО:",
                        value = "${person.firstName} ${person.lastName}",
                        dialogColors = dialogColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Место регистрации
                    InfoRow(
                        label = "Место регистрации:",
                        value = person.registrationPlace.ifEmpty { "Не указано" },
                        dialogColors = dialogColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Место работы
                    InfoRow(
                        label = "Место работы:",
                        value = workplace,
                        dialogColors = dialogColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Здоровье
                    val healthStatus = if (person.health == "здоров") "Здоров" else "Болен: ${person.health}"
                    val healthColor = if (person.health == "здоров") Color(0xFF4CAF50) else Color(0xFFF44336)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Здоровье:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = healthStatus,
                            color = healthColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Дела в полиции
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Дела в полиции:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = if (casesAsSuspect.isNotEmpty()) {
                                "Подозреваемый по ${casesAsSuspect.size} дел(у)"
                            } else {
                                "Не проходит"
                            },
                            color = if (casesAsSuspect.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (casesAsSuspect.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        casesAsSuspect.forEach { case ->
                            VengText(
                                text = "  • Дело №${case.id}: ${case.violationArticle}",
                                color = dialogColors.borderLight.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Банковский счёт
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Банковский счёт:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = if (hasBankAccount) {
                                "Есть (${String.format("%.2f", bankBalance ?: 0.0)} ₽)"
                            } else {
                                "Нет"
                            },
                            color = if (hasBankAccount) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Кнопка закрытия
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Закрыть",
                        modifier = Modifier.width(200.dp),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    dialogColors: DialogColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VengText(
            text = label,
            color = dialogColors.borderLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        VengText(
            text = value,
            color = dialogColors.borderLight.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
    }
}

