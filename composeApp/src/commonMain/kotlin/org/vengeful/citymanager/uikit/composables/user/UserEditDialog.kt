package org.vengeful.citymanager.uikit.composables.user

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.person.SteampunkRightsMultiSelect
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun UserEditDialog(
    user: User,
    persons: List<Person>,
    onDismiss: () -> Unit,
    onSave: (User, String?, Int?) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var username by remember { mutableStateOf(user.username) }
    var password by remember { mutableStateOf("") }
    var selectedRights by remember { mutableStateOf(user.rights.toSet()) }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var personDropdownExpanded by remember { mutableStateOf(false) }

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .width(500.dp)
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
                Text(
                    text = "РЕДАКТИРОВАТЬ ПОЛЬЗОВАТЕЛЯ",
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
                    value = user.id.toString(),
                    onValueChange = { },
                    label = "ИДЕНТИФИКАТОР",
                    placeholder = "ID",
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Username
                VengTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "ИМЯ ПОЛЬЗОВАТЕЛЯ",
                    placeholder = "Введите имя пользователя...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password (опционально)
                VengTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "НОВЫЙ ПАРОЛЬ (ОПЦИОНАЛЬНО)",
                    placeholder = "Оставьте пустым, чтобы не менять...",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Права доступа
                SteampunkRightsMultiSelect(
                    selectedRights = selectedRights,
                    onRightsSelected = { selectedRights = it },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор связанного Person
                Box {
                    val selectedPerson = persons.find { it.id == selectedPersonId }
                    VengTextField(
                        value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" } ?: "Не выбран",
                        onValueChange = { },
                        label = "СВЯЗАННЫЙ ЖИТЕЛЬ",
                        placeholder = "Нажмите для выбора...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { personDropdownExpanded = true },
                        enabled = false,
                        theme = theme
                    )

                    // Кастомная стрелка
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp, top = 20.dp)
                            .clickable { personDropdownExpanded = true }
                    ) {
                        Text(
                            text = if (personDropdownExpanded) "▲" else "▼",
                            color = textFieldColors.text,
                            fontSize = 12.sp
                        )
                    }

                    DropdownMenu(
                        expanded = personDropdownExpanded,
                        onDismissRequest = { personDropdownExpanded = false },
                        modifier = Modifier
                            .background(textFieldColors.background)
                            .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                            .width(450.dp)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                selectedPersonId = null
                                personDropdownExpanded = false
                            },
                            modifier = Modifier.background(textFieldColors.background),
                            text = {
                                Text(
                                    text = "Не выбран",
                                    color = textFieldColors.text,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        )
                        persons.forEach { person ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedPersonId = person.id
                                    personDropdownExpanded = false
                                },
                                modifier = Modifier.background(textFieldColors.background),
                                text = {
                                    Text(
                                        text = "${person.firstName} ${person.lastName} (ID: ${person.id})",
                                        color = textFieldColors.text,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки действий
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "ОТМЕНА",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            val updatedUser = User(
                                id = user.id,
                                username = username,
                                passwordHash = user.passwordHash, // Будет обновлен на сервере, если пароль указан
                                rights = selectedRights.toList(),
                                isActive = user.isActive,
                                createdAt = user.createdAt
                            )
                            val passwordToUpdate = password.ifBlank { null }
                            onSave(updatedUser, passwordToUpdate, selectedPersonId)
                            onDismiss()
                        },
                        text = "СОХРАНИТЬ",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = username.isNotBlank() && selectedRights.isNotEmpty(),
                        theme = theme
                    )
                }
            }
        }
    }
}