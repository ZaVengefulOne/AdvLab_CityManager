package org.vengeful.citymanager.uikit.composables.court

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.composables.court.HearingCard

@Composable
fun HearingListDialog(
    hearings: List<Hearing>,
    onDismiss: () -> Unit,
    onHearingClick: (Hearing) -> Unit,
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    var searchQuery by remember { mutableStateOf("") }
    
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
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = "Список слушаний",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Поле поиска
                    VengTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Поиск слушаний",
                        placeholder = "Введите номер слушания, номер дела, истца или вердикт...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        theme = theme
                    )

                    // Фильтрация слушаний
                    val filteredHearings = remember(hearings, searchQuery) {
                        if (searchQuery.isBlank()) {
                            hearings
                        } else {
                            val searchText = searchQuery.lowercase()
                            hearings.filter { hearing ->
                                "${hearing.id} ${hearing.caseId} ${hearing.plaintiffName} ${hearing.verdict} ${hearing.protocol}".lowercase().contains(searchText)
                            }
                        }
                    }

                    if (filteredHearings.isEmpty()) {
                        VengText(
                            text = if (searchQuery.isBlank()) "Слушания отсутствуют" else "Слушания не найдены",
                            color = dialogColors.borderLight.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredHearings) { hearing ->
                                HearingCard(
                                    hearing = hearing,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onHearingClick(hearing) },
                                    theme = theme
                                )
                            }
                        }
                    }
                }

                // Кнопка закрыть
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Закрыть",
                        modifier = Modifier
                            .weight(1f),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}

