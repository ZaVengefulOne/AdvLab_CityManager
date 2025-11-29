package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.BUILD_VERSION
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkCardColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.DateFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun PersonCard(
    person: Person,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    isExpanded: Boolean = false,
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
            .clickable(enabled = onCardClick != null) { onCardClick?.invoke() }
            .padding(16.dp)
    ) {
        if (isExpanded) {
            ExpandedPersonCardContent(person, cardColors)
        } else {
            CompactPersonCardContent(person, cardColors)
        }
    }
}

@Composable
private fun CompactPersonCardContent(person: Person, colors: SeveritepunkCardColors) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок карточки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VengText(
                text = "ID: ${person.id}",
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(colors.accent, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                VengText(
                    text = "👁️",
                    color = colors.background,
                    fontSize = 10.sp
                )
            }
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
                            colors.accent,
                            Color.Transparent
                        )
                    )
                )
        )

        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VengText(
                text = "${person.firstName} ${person.lastName}",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Краткий список прав (первые 2)
            val displayedRights = person.rights.take(2)
            val remainingCount = person.rights.size - 2

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                displayedRights.forEach { right ->
                    VengText(
                        text = "• ${right.name}",
                        color = colors.accent,
                        fontSize = 10.sp
                    )
                }

                if (remainingCount > 0) {
                    VengText(
                        text = "+$remainingCount ещё...",
                        color = colors.text.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // Декоративные элементы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colors.rivets, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ExpandedPersonCardContent(person: Person, colors: SeveritepunkCardColors) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок
        VengText(
            text = "Профиль жителя",
            color = colors.accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Основная информация
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(label = "Идентификатор:", value = person.id.toString(), cardColors = colors)
            InfoRow(label = "Фамилия:", value = person.lastName, cardColors = colors)
            InfoRow(label = "Имя:", value = person.firstName, cardColors = colors)
        }

        // Права доступа
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            VengText(
                text = "Права доступа:",
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(person.rights) { right ->
                    Box(
                        modifier = Modifier
                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        VengText(
                            text = right.name,
                            color = colors.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

//        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//            Text(
//                text = "ДАТА РЕГИСТРАЦИИ:",
//                color = colors.accent,
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Bold
//            )
//            Text(
//                text = DateFormatter.formatTo1950Date(),
//                color = colors.text,
//                fontSize = 11.sp,
//                fontStyle = FontStyle.Italic
//            )
//        }

        // Декоративный разделитель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.accent.copy(alpha = 0.3f))
        )

        // Подпись системы
        VengText(
            text = stringResource(Res.string.app_name, BUILD_VERSION),
            color = colors.text.copy(alpha = 0.6f),
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, cardColors: SeveritepunkCardColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        VengText(
            text = label,
            color = cardColors.text.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
        VengText(
            text = value,
            color = cardColors.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
