package org.vengeful.citymanager.uikit.composables.bank

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
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Suppress("DefaultLocale")
@Composable
fun BankAccountDialog(
    persons: List<Person>,
    onDismiss: () -> Unit,
    onCreateAccount: (Int?, String?, Double, Double?) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var creditAmount by remember { mutableStateOf("0.0") }
    var enterpriseName by remember { mutableStateOf("") }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var isEnterprise by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }
    var personBalance by remember { mutableStateOf("") }

    val selectedPerson = remember(selectedPersonId) {  // ДОБАВЛЕНО: запоминаем выбранную персону
        persons.find { it.id == selectedPersonId }
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
                            colors = listOf(
                                dialogColors.surface,
                                dialogColors.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                VengText(
                    text = "Открыть банковский счёт",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Переключатель типа счета
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengButton(
                        onClick = {
                            isEnterprise = false
                            selectedPersonId = null
                        },
                        text = "Житель",
                        modifier = Modifier.weight(1f),
                        padding = 10.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            isEnterprise = true
                            selectedPersonId = null
                        },
                        text = "Предприятие",
                        modifier = Modifier.weight(1f),
                        padding = 10.dp,
                        theme = theme
                    )
                }

                // Выбор Person (только если не предприятие)
                if (!isEnterprise) {
                    Box {
                        VengTextField(
                            value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                ?: "Выберите жителя...",
                            onValueChange = { },
                            label = "Житель",
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
                            VengText(
                                text = if (personDropdownExpanded) "▲" else "▼",
                                color = textFieldColors.text,
                                fontSize = 12.sp
                            )
                        }

                        DropdownMenu(
                            expanded = personDropdownExpanded,
                            onDismissRequest = {
                                personDropdownExpanded = false
                                personSearchQuery = ""
                            },
                            modifier = Modifier
                                .background(textFieldColors.background)
                                .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                                .width(350.dp)
                        ) {
                            VengTextField(
                                value = personSearchQuery,
                                onValueChange = { personSearchQuery = it },
                                label = "Поиск",
                                placeholder = "Введите имя, фамилию или ID...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                theme = theme
                            )
                            val filteredPersons = persons.filter { person ->
                                val searchText = personSearchQuery.lowercase()
                                "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                            }

                            filteredPersons.forEach { person ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedPersonId = person.id
                                        personDropdownExpanded = false
                                        personSearchQuery = ""
                                    },
                                    modifier = Modifier.background(textFieldColors.background),
                                    text = {
                                        VengText(
                                            text = "${person.firstName} ${person.lastName} (ID: ${person.id})",
                                            color = textFieldColors.text,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedPerson != null) {
                        VengTextField(
                            value = String.format("%.2f", selectedPerson.balance),
                            onValueChange = { },
                            label = "Текущий баланс жителя",
                            placeholder = "0.00",
                            enabled = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            theme = theme
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        VengTextField(
                            value = personBalance,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    personBalance = newValue
                                }
                            },
                            label = "Баланс жителя",
                            placeholder = "Введите баланс...",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            theme = theme
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // Информация о предприятии
                    Spacer(modifier = Modifier.height(12.dp))

                    // Поле для названия предприятия
                    VengTextField(
                        value = enterpriseName,
                        onValueChange = { enterpriseName = it },
                        label = "Название предприятия",
                        placeholder = "Введите название предприятия...",
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Размер кредита
                VengTextField(
                    value = creditAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            creditAmount = newValue
                        }
                    },
                    label = if (isEnterprise) "Баланс предприятия" else "Размер кредита",
                    placeholder = "0.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
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
                            val credit = creditAmount.ifBlank { "0.0" }.toDoubleOrNull() ?: 0.0
                            val balance = if (!isEnterprise && personBalance.isNotBlank()) {
                                personBalance.toDoubleOrNull()
                            } else {
                                null
                            }

                            if (credit >= 0 && (isEnterprise || selectedPersonId != null)) {
                                val personId = if (isEnterprise) null else selectedPersonId
                                val name = if (isEnterprise) enterpriseName.ifBlank { null } else null
                                onCreateAccount(personId, name, credit, balance) // Передаем баланс
                                onDismiss()
                            }
                        },
                        text = "Открыть",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = run {
                            val creditValid = creditAmount.isBlank() || creditAmount.toDoubleOrNull() != null
                            val creditNonNegative =
                                creditAmount.isBlank() || (creditAmount.toDoubleOrNull() ?: -1.0) >= 0
                            val enterpriseNameValid = !isEnterprise || enterpriseName.isNotBlank()
                            val ownerValid = isEnterprise || selectedPersonId != null
                            creditValid && creditNonNegative && ownerValid && enterpriseNameValid
                        },
                        theme = theme
                    )
                }
            }
        }
    }
}
