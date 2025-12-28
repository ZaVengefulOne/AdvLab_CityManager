package org.vengeful.citymanager.uikit.composables.police

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun PoliceRecordCard(
    modifier: Modifier = Modifier,
    person: Person,
    policeRecord: PoliceRecord? = null,
    onCardClick: (() -> Unit)? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

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
            VengText(
                text = "${person.firstName} ${person.lastName}",
                color = cardColors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            policeRecord?.let { record ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VengText(
                        text = "Возраст: ${record.age}",
                        color = cardColors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (record.workplace.isNotBlank()) {
                    VengText(
                        text = "Место работы: ${record.workplace}",
                        color = cardColors.text,
                        fontSize = 12.sp
                    )
                }
            }

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

            VengText(
                text = "ID: ${person.id}",
                color = cardColors.accent,
                fontSize = 12.sp
            )
        }
    }
}

