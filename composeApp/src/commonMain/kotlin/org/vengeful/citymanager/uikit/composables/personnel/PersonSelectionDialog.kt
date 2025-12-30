package org.vengeful.citymanager.uikit.composables.personnel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun PersonSelectionDialog(
    allPersons: List<Person>,
    onDismiss: () -> Unit,
    onPersonSelected: (Person) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
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

    val textFieldColors = remember(theme) {
        SeveritepunkThemes.getTextFieldColors(theme)
    }

    val filteredPersons = remember(searchQuery, allPersons) {
        if (searchQuery.isBlank()) {
            allPersons
        } else {
            val query = searchQuery.lowercase()
            allPersons.filter { person ->
                "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(query)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .fillMaxWidth(0.9f)
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
                    .padding(24.dp)
            ) {
                VengText(
                    text = "Выберите жителя",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                VengTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Поиск",
                    placeholder = "Введите имя, фамилию или ID...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    theme = theme
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredPersons.isEmpty()) {
                        item {
                            VengText(
                                text = if (searchQuery.isBlank()) "Нет доступных жителей" else "Ничего не найдено",
                                color = dialogColors.borderLight.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        items(filteredPersons) { person ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPersonSelected(person)
                                        onDismiss()
                                    }
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                dialogColors.borderLight.copy(alpha = 0.5f),
                                                dialogColors.borderDark.copy(alpha = 0.5f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                color = dialogColors.surface.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        VengText(
                                            text = "${person.firstName} ${person.lastName}",
                                            color = dialogColors.borderLight,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        VengText(
                                            text = "ID: ${person.id}",
                                            color = dialogColors.borderLight.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VengButton(
                    onClick = onDismiss,
                    text = "Отмена",
                    modifier = Modifier.fillMaxWidth(),
                    padding = 12.dp,
                    theme = theme
                )
            }
        }
    }
}

