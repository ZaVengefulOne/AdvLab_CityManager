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
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Suppress("DefaultLocale")
@Composable
fun BankAccountEditDialog(
    account: BankAccount,
    person: Person?,
    onDismiss: () -> Unit,
    onSave: (BankAccount, Double?) -> Unit,
    onDelete: (Int) -> Unit,
    onCloseCredit: (Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val isEnterpriseAccount = account.personId == null

    var creditAmount by remember { mutableStateOf(account.creditAmount.toString()) }
    var enterpriseName by remember { mutableStateOf(account.enterpriseName ?: "") }
    var personBalance by remember { mutableStateOf(person?.balance?.toString() ?: "") }
    var balanceWasManuallyChanged by remember { mutableStateOf(false) }
    var showCreditWarning by remember { mutableStateOf(false) }

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

                VengTextField(
                    value = account.personId?.toString() ?: "Предприятие",
                    onValueChange = { },
                    label = "Владелец",
                    placeholder = "ID жителя или Предприятие",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(12.dp))

                // Для личного счета - баланс жителя и кредит
                if (!isEnterpriseAccount && person != null) {
                    // Баланс жителя
                    VengTextField(
                        value = personBalance,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                personBalance = newValue
                                balanceWasManuallyChanged = true
                            }
                        },
                        label = "Баланс жителя",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Размер кредита (часть баланса)
                    VengTextField(
                        value = creditAmount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                creditAmount = newValue
                                if (newValue.toDoubleOrNull() != null) {
                                    val newCredit = newValue.toDoubleOrNull() ?: 0.0
                                    val creditDiff = newCredit - account.creditAmount
                                    showCreditWarning = creditDiff != 0.0
                                } else {
                                    showCreditWarning = false
                                }
                            }
                        },
                        label = "Размер кредита",
                        placeholder = "0.0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )

                    if (account.creditAmount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        VengButton(
                            onClick = {
                                onCloseCredit(account.id)
                                onDismiss()
                            },
                            text = "Закрыть кредит",
                            modifier = Modifier.fillMaxWidth(),
                            padding = 12.dp,
                            enabled = person.balance >= account.creditAmount,
                            theme = theme
                        )
                    }
                }

                // Для счета предприятия - название и баланс
                if (isEnterpriseAccount) {
                    VengTextField(
                        value = enterpriseName,
                        onValueChange = { enterpriseName = it },
                        label = "НАЗВАНИЕ ПРЕДПРИЯТИЯ",
                        placeholder = "Введите название предприятия...",
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Баланс предприятия
                    VengTextField(
                        value = creditAmount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                creditAmount = newValue
                            }
                        },
                        label = "Баланс предприятия",
                        placeholder = "0.0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )
                }

                if (showCreditWarning && !isEnterpriseAccount) {
                    Spacer(modifier = Modifier.height(8.dp))
                    VengText(
                        text = "⚠ Изменение кредита изменит баланс жителя",
                        color = Color(0xFFFFA500),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                val credit = creditAmount.toDoubleOrNull() ?: 0.0
                                val balance = if (!isEnterpriseAccount && balanceWasManuallyChanged && personBalance.isNotBlank()) {
                                    personBalance.toDoubleOrNull()
                                } else {
                                    null
                                }
                                if (credit >= 0 && (balance == null || balance >= 0)) {
                                    val updatedAccount = BankAccount(
                                        id = account.id,
                                        personId = account.personId,
                                        enterpriseName = if (isEnterpriseAccount) {
                                            enterpriseName.ifBlank { account.enterpriseName }
                                        } else {
                                            null
                                        },
                                        creditAmount = credit
                                    )
                                    onSave(updatedAccount, balance)
                                    onDismiss()
                                }
                            },
                            text = "Сохранить",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            padding = 12.dp,
                            enabled = creditAmount.toDoubleOrNull() != null &&
                                creditAmount.toDoubleOrNull()!! >= 0 &&
                                (personBalance.isEmpty() || personBalance.toDoubleOrNull() != null && personBalance.toDoubleOrNull()!! >= 0),
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}
