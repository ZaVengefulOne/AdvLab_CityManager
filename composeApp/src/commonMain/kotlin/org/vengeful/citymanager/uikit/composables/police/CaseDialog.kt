package org.vengeful.citymanager.uikit.composables.police

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun CaseDialog(
    persons: List<Person>,
    investigatorPersonId: Int,
    investigatorName: String,
    onDismiss: () -> Unit,
    onCreateCase: (Case) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var complainantPersonId by remember { mutableStateOf<Int?>(null) }
    var complainantName by remember { mutableStateOf("") }
    var complainantManualInput by remember { mutableStateOf(false) }
    var complainantDropdownExpanded by remember { mutableStateOf(false) }
    var complainantSearchQuery by remember { mutableStateOf("") }

    var suspectPersonId by remember { mutableStateOf<Int?>(null) }
    var suspectName by remember { mutableStateOf("") }
    var suspectManualInput by remember { mutableStateOf(false) }
    var suspectDropdownExpanded by remember { mutableStateOf(false) }
    var suspectSearchQuery by remember { mutableStateOf("") }

    var statementText by remember { mutableStateOf("") }
    var violationArticle by remember { mutableStateOf("") }

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
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
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
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = "Создать дело",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Следователь (только для отображения)
                    VengText(
                        text = "Следователь: $investigatorName (ID: $investigatorPersonId)",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Заявитель
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = complainantManualInput,
                            onCheckedChange = {
                                complainantManualInput = it
                                if (it) {
                                    complainantPersonId = null
                                    complainantName = ""
                                } else {
                                    complainantName = ""
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = dialogColors.borderLight,
                                uncheckedColor = dialogColors.borderLight.copy(alpha = 0.6f),
                                checkmarkColor = dialogColors.background
                            )
                        )
                        VengText(
                            text = "Ввести вручную",
                            color = dialogColors.borderLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (!complainantManualInput) {
                        Box {
                            val selectedPerson = persons.find { it.id == complainantPersonId }
                            VengTextField(
                                value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                    ?: "Выберите заявителя...",
                                onValueChange = { },
                                label = "Заявитель",
                                placeholder = "Нажмите для выбора...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { complainantDropdownExpanded = true },
                                enabled = false,
                                theme = theme
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp, top = 20.dp)
                                    .clickable { complainantDropdownExpanded = true }
                            ) {
                                VengText(
                                    text = if (complainantDropdownExpanded) "▲" else "▼",
                                    color = textFieldColors.text,
                                    fontSize = 12.sp
                                )
                            }

                            DropdownMenu(
                                expanded = complainantDropdownExpanded,
                                onDismissRequest = {
                                    complainantDropdownExpanded = false
                                    complainantSearchQuery = ""
                                },
                                modifier = Modifier
                                    .background(textFieldColors.background)
                                    .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                                    .width(350.dp)
                            ) {
                                VengTextField(
                                    value = complainantSearchQuery,
                                    onValueChange = { complainantSearchQuery = it },
                                    label = "Поиск",
                                    placeholder = "Введите имя, фамилию или ID...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    theme = theme
                                )
                                val filteredPersons = persons.filter { person ->
                                    val searchText = complainantSearchQuery.lowercase()
                                    "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                                }

                                filteredPersons.forEach { person ->
                                    DropdownMenuItem(
                                        onClick = {
                                            complainantPersonId = person.id
                                            complainantName = "${person.firstName} ${person.lastName}"
                                            complainantDropdownExpanded = false
                                            complainantSearchQuery = ""
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
                    } else {
                        VengTextField(
                            value = complainantName,
                            onValueChange = { complainantName = it },
                            label = "Имя заявителя",
                            placeholder = "Введите имя заявителя...",
                            modifier = Modifier.fillMaxWidth(),
                            theme = theme
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Подозреваемый
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = suspectManualInput,
                            onCheckedChange = {
                                suspectManualInput = it
                                if (it) {
                                    suspectPersonId = null
                                    suspectName = ""
                                } else {
                                    suspectName = ""
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = dialogColors.borderLight,
                                uncheckedColor = dialogColors.borderLight.copy(alpha = 0.6f),
                                checkmarkColor = dialogColors.background
                            )
                        )
                        VengText(
                            text = "Ввести вручную",
                            color = dialogColors.borderLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (!suspectManualInput) {
                        Box {
                            val selectedPerson = persons.find { it.id == suspectPersonId }
                            VengTextField(
                                value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                    ?: "Выберите подозреваемого...",
                                onValueChange = { },
                                label = "Подозреваемый",
                                placeholder = "Нажмите для выбора...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { suspectDropdownExpanded = true },
                                enabled = false,
                                theme = theme
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp, top = 20.dp)
                                    .clickable { suspectDropdownExpanded = true }
                            ) {
                                VengText(
                                    text = if (suspectDropdownExpanded) "▲" else "▼",
                                    color = textFieldColors.text,
                                    fontSize = 12.sp
                                )
                            }

                            DropdownMenu(
                                expanded = suspectDropdownExpanded,
                                onDismissRequest = {
                                    suspectDropdownExpanded = false
                                    suspectSearchQuery = ""
                                },
                                modifier = Modifier
                                    .background(textFieldColors.background)
                                    .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                                    .width(350.dp)
                            ) {
                                VengTextField(
                                    value = suspectSearchQuery,
                                    onValueChange = { suspectSearchQuery = it },
                                    label = "Поиск",
                                    placeholder = "Введите имя, фамилию или ID...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    theme = theme
                                )
                                val filteredPersons = persons.filter { person ->
                                    val searchText = suspectSearchQuery.lowercase()
                                    "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                                }

                                filteredPersons.forEach { person ->
                                    DropdownMenuItem(
                                        onClick = {
                                            suspectPersonId = person.id
                                            suspectName = "${person.firstName} ${person.lastName}"
                                            suspectDropdownExpanded = false
                                            suspectSearchQuery = ""
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
                    } else {
                        VengTextField(
                            value = suspectName,
                            onValueChange = { suspectName = it },
                            label = "Имя подозреваемого",
                            placeholder = "Введите имя подозреваемого...",
                            modifier = Modifier.fillMaxWidth(),
                            theme = theme
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Текст заявления
                    VengTextField(
                        value = statementText,
                        onValueChange = { statementText = it },
                        label = "Текст заявления",
                        placeholder = "Введите текст заявления...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        theme = theme,
                        maxLines = 10
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Статья правонарушения
                    VengTextField(
                        value = violationArticle,
                        onValueChange = { violationArticle = it },
                        label = "Статья правонарушения",
                        placeholder = "Введите статью правонарушения...",
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )

                }

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
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
                            if (validateInput(complainantName, suspectName, statementText, violationArticle)) {
                                val case = Case(
                                    complainantPersonId = complainantPersonId,
                                    complainantName = complainantName,
                                    investigatorPersonId = investigatorPersonId,
                                    suspectPersonId = suspectPersonId,
                                    suspectName = suspectName,
                                    statementText = statementText,
                                    violationArticle = violationArticle,
                                    status = CaseStatus.OPEN
                                )
                                onCreateCase(case)
                                onDismiss()
                            }
                        },
                        text = "Создать",
                        enabled = validateInput(complainantName, suspectName, statementText, violationArticle),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}

private fun validateInput(
    complainantName: String,
    suspectName: String,
    statementText: String,
    violationArticle: String
): Boolean {
    return complainantName.isNotBlank() &&
        suspectName.isNotBlank() &&
        statementText.isNotBlank() &&
        violationArticle.isNotBlank()
}

