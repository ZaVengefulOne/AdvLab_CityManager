package org.vengeful.citymanager.uikit.composables.medic


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.vengeful.citymanager.models.medicine.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.DateFormatter

@Composable
fun MedicalRecordDialog(
    persons: List<Person>,
    onDismiss: () -> Unit,
    onCreateRecord: (MedicalRecord, String) -> Unit, // record и healthStatus
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) } // "M" или "Ж"
    var dateOfBirthText by remember { mutableStateOf("") } // Формат: DD.MM.YYYY
    var workplace by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var healthStatus by remember { mutableStateOf("здоров") }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }
    var textFieldFocused by remember { mutableStateOf(false) }

    val medicPersons = remember(persons) {
        persons.filter { it.rights.contains(Rights.Medic) }
    }

    var selectedDoctorId by remember { mutableStateOf<Int?>(null) }
    var doctorDropdownExpanded by remember { mutableStateOf(false) }
    var doctorSearchQuery by remember { mutableStateOf("") }
    var isCustomDoctor by remember { mutableStateOf(false) }

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
                    text = "Медицинская карточка пациента",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )


                Box {
                    val selectedPerson = persons.find { it.id == selectedPersonId }
                    VengTextField(
                        value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                            ?: "Выберите пациента...",
                        onValueChange = { },
                        label = "Пациент",
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
                                    firstName = person.firstName
                                    lastName = person.lastName
                                    workplace =
                                        person.rights.first { rights -> rights != Rights.Any && rights != Rights.Joker }
                                            .getDisplayName()
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

                // ФИО (теперь заполняются автоматически, но можно редактировать)
                VengTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Имя",
                    placeholder = "Введите имя...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Фамилия",
                    placeholder = "Введите фамилию...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Пол
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Checkbox(
                        checked = gender == "M",
                        onCheckedChange = {
                            gender = if (it) "M" else null
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = dialogColors.borderLight
                        )
                    )
                    VengText(
                        text = "М",
                        color = dialogColors.borderLight,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Checkbox(
                        checked = gender == "Ж",
                        onCheckedChange = {
                            gender = if (it) "Ж" else null
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = dialogColors.borderLight
                        )
                    )
                    VengText(
                        text = "Ж",
                        color = dialogColors.borderLight,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Дата рождения
                VengTextField(
                    value = dateOfBirthText,
                    onValueChange = { dateOfBirthText = it },
                    label = "Дата рождения",
                    placeholder = "DD.MM.YYYY",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Место работы
                VengTextField(
                    value = workplace,
                    onValueChange = { workplace = it },
                    label = "Место работы",
                    placeholder = "Введите место работы...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Лечащий врач
                Box {
                    val selectedDoctor = medicPersons.find { it.id == selectedDoctorId }
                    VengTextField(
                        value = if (isCustomDoctor) {
                            doctor
                        } else {
                            selectedDoctor?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                ?: "Выберите врача или введите ФИО..."
                        },
                        onValueChange = {
                            doctor = it
                            isCustomDoctor = true
                            selectedDoctorId = null
                        },
                        label = "Лечащий врач",
                        placeholder = "Выберите из списка или введите ФИО...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCustomDoctor) {
                                doctorDropdownExpanded = true
                            },
                        enabled = isCustomDoctor,
                        theme = theme
                    )

                    if (!isCustomDoctor) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp, top = 20.dp)
                                .clickable { doctorDropdownExpanded = true }
                        ) {
                            VengText(
                                text = if (doctorDropdownExpanded) "▲" else "▼",
                                color = textFieldColors.text,
                                fontSize = 12.sp
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = doctorDropdownExpanded,
                        onDismissRequest = {
                            doctorDropdownExpanded = false
                            doctorSearchQuery = ""
                        },
                        modifier = Modifier
                            .background(textFieldColors.background)
                            .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                            .width(350.dp)
                    ) {
                        VengTextField(
                            value = doctorSearchQuery,
                            onValueChange = { doctorSearchQuery = it },
                            label = "Поиск",
                            placeholder = "Введите имя, фамилию или ID...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            theme = theme
                        )
                        val filteredDoctors = medicPersons.filter { person ->
                            val searchText = doctorSearchQuery.lowercase()
                            "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                        }

                        DropdownMenuItem(
                            onClick = {
                                isCustomDoctor = true
                                doctor = ""
                                selectedDoctorId = null
                                doctorDropdownExpanded = false
                                doctorSearchQuery = ""
                            },
                            modifier = Modifier.background(textFieldColors.background),
                            text = {
                                VengText(
                                    text = "Ввести ФИО вручную",
                                    color = textFieldColors.text,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        )

                        filteredDoctors.forEach { person ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedDoctorId = person.id
                                    doctor = "${person.firstName} ${person.lastName}"
                                    isCustomDoctor = false
                                    doctorDropdownExpanded = false
                                    doctorSearchQuery = ""
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

                // Статус здоровья
                VengTextField(
                    value = healthStatus,
                    onValueChange = { healthStatus = it },
                    label = "Статус здоровья",
                    placeholder = "здоров или название болезни",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки
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
                            if (validateInput(
                                    firstName,
                                    lastName,
                                    gender,
                                    dateOfBirthText,
                                    workplace,
                                    doctor
                                ) && selectedPersonId != null
                            ) {
                                val dateOfBirth = DateFormatter.formatDateTimeTo1950(dateOfBirthText)
                                val record = MedicalRecord(
                                    personId = selectedPersonId!!, // ИСПОЛЬЗУЕМ выбранный ID
                                    firstName = firstName,
                                    lastName = lastName,
                                    gender = gender ?: "",
                                    dateOfBirth = dateOfBirth,
                                    workplace = workplace,
                                    doctor = doctor
                                )
                                onCreateRecord(record, healthStatus)
                                onDismiss()
                            }
                        },
                        text = "Создать",
                        enabled = validateInput(
                            firstName,
                            lastName,
                            gender,
                            dateOfBirthText,
                            workplace,
                            doctor
                        ) && selectedPersonId != null,
                        theme = theme
                    )
                }
            }
        }
    }
}

private fun validateInput(
    firstName: String,
    lastName: String,
    gender: String?,
    dateOfBirth: String,
    workplace: String,
    doctor: String
): Boolean {
    return firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        gender != null &&
        dateOfBirth.isNotBlank() &&
        workplace.isNotBlank() &&
        doctor.isNotBlank()
}
