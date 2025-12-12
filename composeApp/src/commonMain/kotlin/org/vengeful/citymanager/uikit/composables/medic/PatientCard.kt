package org.vengeful.citymanager.uikit.composables.medic


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.medicine.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun PatientCard(
    modifier: Modifier = Modifier,
    person: Person,
    medicalRecord: MedicalRecord? = null,
    onCardClick: (() -> Unit)? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

    val isHealthy = person.health == "здоров"
    val healthIndicatorColor = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardColors.background)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(cardColors.borderLight, cardColors.borderDark)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable(
                onClick = onCardClick ?: { }
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ИСПРАВЛЕНО: Имя на отдельной строке
            VengText(
                text = "${person.firstName} ${person.lastName}",
                color = cardColors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // ИСПРАВЛЕНО: Статус здоровья на следующей строке, без ограничений на текст
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(healthIndicatorColor, CircleShape)
                )
                VengText(
                    text = if (isHealthy) "Здоров" else person.health,
                    color = healthIndicatorColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Разделитель
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cardColors.accent,
                                Color.Transparent
                            )
                        )
                    )
            )

            medicalRecord?.prescribedTreatment?.takeIf { it.isNotBlank() }?.let { treatment ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VengText(
                        text = "Лечение:",
                        color = cardColors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    VengText(
                        text = treatment,
                        color = cardColors.text,
                        fontSize = 11.sp,
                        maxLines = 3 // Ограничиваем 3 строками для компактности
                    )
                }
            }

            VengText(
                text = "ID: ${person.id}",
                color = cardColors.accent,
                fontSize = 12.sp
            )
        }
    }
}
