package org.vengeful.citymanager.uikit.composables.dialogs


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
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegister: (String, String, Int?) -> Unit,
    persons: List<Person> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
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
                            colors = listOf(dialogColors.surface, dialogColors.background)
                        )
                    )
                    .padding(24.dp)
            ) {
                // Заголовок
                Text(
                    text = "РЕГИСТРАЦИЯ",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Создайте новый аккаунт",
                    color = dialogColors.borderLight.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Поле username
                VengTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "ИМЯ ПОЛЬЗОВАТЕЛЯ",
                    placeholder = "Введите логин...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле password
                VengTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "ПАРОЛЬ",
                    placeholder = "Введите пароль...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле confirmPassword
                VengTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "ПОДТВЕРЖДЕНИЕ ПАРОЛЯ",
                    placeholder = "Повторите пароль...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown для выбора Person
                Box {
                    VengTextField(
                        value = selectedPerson?.let { "${it.firstName} ${it.lastName}" } ?: "",
                        onValueChange = { },
                        label = "ВЫБЕРИТЕ ЧЕЛОВЕКА",
                        placeholder = "Выберите из списка...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { personDropdownExpanded = true },
                        theme = theme,
                        enabled = !isLoading && persons.isNotEmpty()
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
                            .width(350.dp)
                    ) {
                        persons.forEach { person ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedPerson = person
                                    personDropdownExpanded = false
                                },
                                modifier = Modifier.background(textFieldColors.background),
                                text = {
                                    Text(
                                        text = "${person.firstName} ${person.lastName}",
                                        color = textFieldColors.text,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Сообщение об ошибке
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // Индикатор загрузки
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = dialogColors.borderLight,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Регистрация...",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

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
                        padding = 14.dp,
                        theme = theme,
                        enabled = !isLoading
                    )

                    VengButton(
                        onClick = {
                            if (username.isNotBlank() &&
                                password.isNotBlank() &&
                                password == confirmPassword &&
                                !isLoading
                            ) {
                                onRegister(username, password, selectedPerson?.id)
                            }
                        },
                        text = "ЗАРЕГИСТРИРОВАТЬ",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 14.dp,
                        enabled = username.isNotBlank() &&
                                password.isNotBlank() &&
                                password == confirmPassword &&
                                password.length >= 3 &&
                                !isLoading,
                        theme = theme
                    )
                }
            }
        }
    }
}