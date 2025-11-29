package org.vengeful.citymanager.uikit.composables.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun BankAccountEditDialog(
    account: BankAccount,
    onDismiss: () -> Unit,
    onSave: (BankAccount) -> Unit,
    onDelete: (Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var depositAmount by remember { mutableStateOf(account.depositAmount.toString()) }
    var creditAmount by remember { mutableStateOf(account.creditAmount.toString()) }
    var enterpriseName by remember { mutableStateOf(account.enterpriseName ?: "") }

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
                    text = "Редактировать банковский счёт",
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
                    value = account.id.toString(),
                    onValueChange = { },
                    label = "Идентификатор счёта",
                    placeholder = "ID",
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Person ID или "Предприятие" (только для отображения)
                VengTextField(
                    value = account.personId?.toString() ?: "Предприятие",
                    onValueChange = { },
                    label = "Владелец",
                    placeholder = "Person ID или Предприятие",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Название предприятия (если это предприятие)
                if (account.personId == null) {
                    VengTextField(
                        value = enterpriseName,
                        onValueChange = { enterpriseName = it },
                        label = "НАЗВАНИЕ ПРЕДПРИЯТИЯ",
                        placeholder = "Введите название предприятия...",
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Размер вклада
                VengTextField(
                    value = depositAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            depositAmount = newValue
                        }
                    },
                    label = "Размер депозита",
                    placeholder = "0.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Размер кредита
                VengTextField(
                    value = creditAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            creditAmount = newValue
                        }
                    },
                    label = "Размер кредита",
                    placeholder = "0.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки действий
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Кнопка удаления
                    VengButton(
                        onClick = {
                            onDelete(account.id)
                            onDismiss()
                        },
                        text = "Закрыть счёт",
                        modifier = Modifier.fillMaxWidth(),
                        padding = 12.dp,
                        theme = theme
                    )

                    // Кнопки отмены и сохранения
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
                                val deposit = depositAmount.toDoubleOrNull() ?: 0.0
                                val credit = creditAmount.toDoubleOrNull() ?: 0.0

                                if (deposit >= 0 && credit >= 0) {
                                    val updatedAccount = BankAccount(
                                        id = account.id,
                                        personId = account.personId,
                                        enterpriseName = if (account.personId == null) enterpriseName.ifBlank { null } else null, // НОВОЕ
                                        depositAmount = deposit,
                                        creditAmount = credit
                                    )
                                    onSave(updatedAccount)
                                    onDismiss()
                                }
                            },
                            text = "Сохранить",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            padding = 12.dp,
                            enabled = depositAmount.toDoubleOrNull() != null &&
                                    creditAmount.toDoubleOrNull() != null &&
                                    depositAmount.toDoubleOrNull()!! >= 0 &&
                                    creditAmount.toDoubleOrNull()!! >= 0,
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}
