package org.vengeful.citymanager.uikit.composables.person

import VengRightsMultiSelect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun PersonEditDialog(
    person: Person,
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var firstName by remember { mutableStateOf(person.firstName) }
    var lastName by remember { mutableStateOf(person.lastName) }
    var selectedRights by remember { mutableStateOf(person.rights.toSet()) }
    var registrationPlace by remember {
        mutableStateOf(person.registrationPlace)
    }
    var registrationPlaceDropdownExpanded by remember { mutableStateOf(false) }

    val popularRegistrationPlaces = remember {
        listOf("Эбони-Бэй", "Лэбтаун", "Техносоюз")
    }

    var isCustomRegistrationPlace by remember {
        mutableStateOf(person.registrationPlace !in popularRegistrationPlaces)
    }

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
                .width(400.dp)
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
                            colors = listOf(
                                dialogColors.surface,
                                dialogColors.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                VengText(
                    text = "Редактировать жителя",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // ID (только для отображения)
                VengTextField(
                    value = person.id.toString(),
                    onValueChange = { },
                    label = "Идентификатор",
                    placeholder = "ID",
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Имя
                VengTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Имя",
                    placeholder = "Введите имя...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Фамилия
                VengTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Фамилия",
                    placeholder = "Введите фамилию...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Поле "Место регистрации"
                Box {
                    VengTextField(
                        value = if (isCustomRegistrationPlace) registrationPlace else registrationPlace,
                        onValueChange = {
                            registrationPlace = it
                            isCustomRegistrationPlace = true
                        },
                        label = "Место регистрации",
                        placeholder = if (isCustomRegistrationPlace) "Введите место регистрации..." else "Выберите или введите...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCustomRegistrationPlace) {
                                registrationPlaceDropdownExpanded = true
                            },
                        enabled = isCustomRegistrationPlace,
                        theme = theme
                    )

                    if (!isCustomRegistrationPlace) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp, top = 20.dp)
                                .clickable { registrationPlaceDropdownExpanded = true }
                        ) {
                            VengText(
                                text = if (registrationPlaceDropdownExpanded) "▲" else "▼",
                                fontSize = 12.sp,
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = registrationPlaceDropdownExpanded,
                        onDismissRequest = { registrationPlaceDropdownExpanded = false },
                        modifier = Modifier
                            .background(dialogColors.surface)
                            .border(2.dp, dialogColors.borderLight, RoundedCornerShape(6.dp))
                            .width(350.dp)
                    ) {
                        popularRegistrationPlaces.forEach { place ->
                            DropdownMenuItem(
                                onClick = {
                                    registrationPlace = place
                                    isCustomRegistrationPlace = false
                                    registrationPlaceDropdownExpanded = false
                                },
                                modifier = Modifier.background(dialogColors.surface),
                                text = {
                                    VengText(
                                        text = place,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            onClick = {
                                isCustomRegistrationPlace = true
                                registrationPlace = ""
                                registrationPlaceDropdownExpanded = false
                            },
                            modifier = Modifier.background(dialogColors.surface),
                            text = {
                                VengText(
                                    text = "Ввести свой вариант",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Права доступа
                VengRightsMultiSelect(
                    selectedRights = selectedRights,
                    onRightsSelected = { selectedRights = it },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки действий
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
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
                            val updatedPerson = Person(
                                id = person.id,
                                firstName = firstName,
                                lastName = lastName,
                                registrationPlace = registrationPlace,
                                rights = selectedRights.toList()
                            )
                            onSave(updatedPerson)
                            onDismiss()
                        },
                        text = "Сохранить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = firstName.isNotBlank() && lastName.isNotBlank() && registrationPlace.isNotBlank() && selectedRights.isNotEmpty(),
                        theme = theme
                    )
                }
            }
        }
    }
}
