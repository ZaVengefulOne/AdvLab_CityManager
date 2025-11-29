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
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun BankAccountDialog(
    persons: List<Person>,
    onDismiss: () -> Unit,
    onCreateAccount: (Int?, String?, Double, Double) -> Unit, // personId теперь nullable
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var depositAmount by remember { mutableStateOf("0.0") }
    var creditAmount by remember { mutableStateOf("0.0") }
    var enterpriseName by remember { mutableStateOf("") }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var isEnterprise by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }

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
                Text(
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
                        val selectedPerson = persons.find { it.id == selectedPersonId }
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
                            Text(
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

                    Spacer(modifier = Modifier.height(12.dp))
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

                // Размер вклада
                VengTextField(
                    value = depositAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            depositAmount = newValue
                        }
                    },
                    label = "РАЗМЕР ВКЛАДА",
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
                            val deposit = depositAmount.ifBlank { "0.0" }.toDoubleOrNull() ?: 0.0
                            val credit = creditAmount.ifBlank { "0.0" }.toDoubleOrNull() ?: 0.0

                            if (deposit >= 0 && credit >= 0 && (isEnterprise || selectedPersonId != null)) {
                                val personId = if (isEnterprise) null else selectedPersonId
                                val name = if (isEnterprise) enterpriseName.ifBlank { null } else null
                                onCreateAccount(personId, name, deposit, credit) // НОВОЕ: передаем название
                                onDismiss()
                            }
                        },
                        text = "Открыть",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = run {
                            val depositValid = depositAmount.isBlank() || depositAmount.toDoubleOrNull() != null
                            val creditValid = creditAmount.isBlank() || creditAmount.toDoubleOrNull() != null
                            val depositNonNegative =
                                depositAmount.isBlank() || (depositAmount.toDoubleOrNull() ?: -1.0) >= 0
                            val creditNonNegative =
                                creditAmount.isBlank() || (creditAmount.toDoubleOrNull() ?: -1.0) >= 0
                            val enterpriseNameValid = !isEnterprise || enterpriseName.isNotBlank()
                            val ownerValid = isEnterprise || selectedPersonId != null
                            depositValid && creditValid && depositNonNegative && creditNonNegative && ownerValid && enterpriseNameValid
                        },
                        theme = theme
                    )
                }
            }
        }
    }
}
