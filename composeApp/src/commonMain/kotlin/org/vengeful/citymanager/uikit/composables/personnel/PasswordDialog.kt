package org.vengeful.citymanager.uikit.composables.personnel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
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
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onPasswordCorrect: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                .width(380.dp)
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                VengText(
                    text = "Введите пароль",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                VengText(
                    text = "Для доступа к управлению персоналом",
                    color = dialogColors.borderLight.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                VengTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = "Пароль",
                    placeholder = "Введите пароль...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (errorMessage != null) {
                    VengText(
                        text = errorMessage!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                        onClick = {
                            if (password == org.vengeful.citymanager.PERSONNEL_MANAGEMENT_PASSWORD) {
                                onPasswordCorrect()
                            } else {
                                errorMessage = "Неверный пароль"
                                password = ""
                            }
                        },
                        text = "Войти",
                        modifier = Modifier.weight(1f),
                        padding = 12.dp,
                        theme = theme,
                        enabled = password.isNotBlank()
                    )
                }
            }
        }
    }
}


