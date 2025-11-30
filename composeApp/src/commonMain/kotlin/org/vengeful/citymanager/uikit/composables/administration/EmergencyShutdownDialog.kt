package org.vengeful.citymanager.uikit.composables.administration


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign


@Composable
fun EmergencyShutdownDialog(
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Int, password: String) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN,
    errorMessage: String? = null
) {
    var durationText by remember { mutableStateOf("10") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

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

    // Обновляем локальное сообщение об ошибке при изменении внешнего
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            localErrorMessage = errorMessage
            // Сбрасываем пароль при ошибке для безопасности
            password = ""
        }
    }

    fun validateAndConfirm() {
        localErrorMessage = null
        val duration = durationText.trim().toIntOrNull()

        when {
            duration == null -> {
                localErrorMessage = "Введите число"
            }
            duration < 1 -> {
                localErrorMessage = "Минимальное время: 1 минута"
            }
            duration > 30 -> {
                localErrorMessage = "Максимальное время: 30 минут"
            }
            password.isBlank() -> {
                localErrorMessage = "Введите пароль"
            }
            else -> {
                onConfirm(duration, password)
                // Диалог закроется только при успешной активации
            }
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VengText(
                    text = "Экстренное отключение",
                    color = Color(0xFFE74C3C),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                VengText(
                    text = "Вы уверены, что хотите активировать экстренное отключение?",
                    color = dialogColors.borderLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                VengText(
                    text = "Все сессии будут заблокированы на указанное время, кроме вашей.",
                    color = dialogColors.borderLight.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Поле ввода времени блокировки
                VengTextField(
                    value = durationText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            durationText = newValue
                            localErrorMessage = null
                        }
                    },
                    label = "Время блокировки (минуты)",
                    placeholder = "10",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )


                VengTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localErrorMessage = null
                    },
                    label = "Пароль экстренного отключения",
                    placeholder = "Введите пароль",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                // Сообщение об ошибке валидации (более заметное)
                if (localErrorMessage != null || errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        VengText(
                            text = localErrorMessage ?: errorMessage ?: "",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }
                }

                // Подсказка о диапазоне
                VengText(
                    text = "Диапазон: от 1 до 30 минут",
                    color = dialogColors.borderLight.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Отмена",
                        modifier = Modifier.weight(1f),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = { validateAndConfirm() },
                        text = "Подтвердить",
                        modifier = Modifier.weight(1f),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}
