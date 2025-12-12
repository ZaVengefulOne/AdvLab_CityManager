package org.vengeful.citymanager.uikit.composables.medic


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
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Suppress("DefaultLocale")
@Composable
fun OrderMedicineDialog(
    medicines: List<Medicine>,
    availableAccounts: List<BankAccount>,
    currentPerson: Person?,
    onDismiss: () -> Unit,
    onOrder: (Int, Int, Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var selectedMedicineId by remember { mutableStateOf<Int?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var medicineDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }

    val selectedMedicine = medicines.find { it.id == selectedMedicineId }
    val quantity = quantityText.toIntOrNull() ?: 0
    val totalPrice = selectedMedicine?.let { it.price * quantity } ?: 0.0

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

    val getAccountBalance: (BankAccount) -> Double = { account ->
        if (account.personId != null && currentPerson?.id == account.personId) {
            currentPerson?.balance ?: 0.0
        } else {
            account.creditAmount
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .width(450.dp)
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
                VengText(
                    text = "Заказ лекарств",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Выбор лекарства
                Box {
                    val selectedMedicine = medicines.find { it.id == selectedMedicineId }
                    VengTextField(
                        value = selectedMedicine?.let { "${it.name} - ${it.price}" } ?: "Выберите лекарство...",
                        onValueChange = { },
                        label = "Название лекарства",
                        placeholder = "Выберите из списка...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { medicineDropdownExpanded = true },
                        enabled = false,
                        theme = theme
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp, top = 20.dp)
                            .clickable { medicineDropdownExpanded = true }
                    ) {
                        VengText(
                            text = if (medicineDropdownExpanded) "▲" else "▼",
                            color = textFieldColors.text,
                            fontSize = 12.sp
                        )
                    }

                    DropdownMenu(
                        expanded = medicineDropdownExpanded,
                        onDismissRequest = { medicineDropdownExpanded = false },
                        modifier = Modifier
                            .background(textFieldColors.background)
                            .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                            .width(400.dp)
                    ) {
                        medicines.forEach { medicine ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedMedicineId = medicine.id
                                    medicineDropdownExpanded = false
                                },
                                modifier = Modifier.background(textFieldColors.background),
                                text = {
                                    VengText(
                                        text = "${medicine.name} - ${medicine.price}",
                                        color = textFieldColors.text,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Количество
                VengTextField(
                    value = quantityText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            quantityText = newValue
                        }
                    },
                    label = "Количество (шт.)",
                    placeholder = "1",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )
                Spacer(modifier = Modifier.height(12.dp))
                VengText(
                    text = "Итоговая цена: $totalPrice",
                    color = dialogColors.borderLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    val selectedAccount = availableAccounts.find { it.id == selectedAccountId }
                    VengTextField(
                        value = selectedAccount?.let {
                            val accountName = if (it.enterpriseName != null) {
                                "Счёт предприятия: ${it.enterpriseName}"
                            } else {
                                "Личный счёт (ID: ${it.personId})"
                            }
                            val balance = if (it.personId != null && currentPerson?.id == it.personId) {
                                currentPerson?.balance ?: 0.0
                            } else {
                                it.creditAmount
                            }
                            "$accountName (Баланс: ${String.format("%.2f", balance)})"
                        } ?: "Выберите счёт для оплаты...",
                        onValueChange = { },
                        label = "Счёт оплаты",
                        placeholder = "Выберите из списка...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountDropdownExpanded = true },
                        enabled = false,
                        theme = theme
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp, top = 20.dp)
                            .clickable { accountDropdownExpanded = true }
                    ) {
                        VengText(
                            text = if (accountDropdownExpanded) "▲" else "▼",
                            color = textFieldColors.text,
                            fontSize = 12.sp
                        )
                    }

                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false },
                        modifier = Modifier
                            .background(textFieldColors.background)
                            .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                            .width(400.dp)
                    ) {
                        availableAccounts.forEach { account ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedAccountId = account.id
                                    accountDropdownExpanded = false
                                },
                                modifier = Modifier.background(textFieldColors.background),
                                text = {
                                    val accountName = if (account.enterpriseName != null) {
                                        "${account.enterpriseName}"
                                    } else {
                                        "${currentPerson?.firstName} ${currentPerson?.lastName}"
                                    }
                                    // Для личного счета используем баланс персоны, для предприятия - creditAmount
                                    val balance = if (account.personId != null && currentPerson?.id == account.personId) {
                                        currentPerson?.balance ?: 0.0
                                    } else {
                                        account.creditAmount
                                    }
                                    VengText(
                                        text = "$accountName (Баланс: ${String.format("%.2f", balance)})",
                                        color = textFieldColors.text,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedAccount = availableAccounts.find { it.id == selectedAccountId }
                    val accountBalance = selectedAccount?.let { getAccountBalance(it) } ?: 0.0

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
                            if (selectedMedicineId != null && quantity > 0 && selectedAccountId != null) {
                                onOrder(selectedMedicineId!!, quantity, selectedAccountId!!)
                                onDismiss()
                            }
                        },
                        text = "Оплатить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = selectedMedicineId != null &&
                            quantity > 0 &&
                            selectedAccountId != null &&
                            accountBalance >= totalPrice,
                        theme = theme
                    )
                }
            }
        }
    }
}
