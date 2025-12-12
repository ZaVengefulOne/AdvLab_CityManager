
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
fun EditMedicalRecordDialog(
    person: Person,
    medicalRecord: MedicalRecord?,
    persons: List<Person>,
    onDismiss: () -> Unit,
    onSave: (Int, MedicalRecord, String) -> Unit,
    onDelete: (Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    // Фильтруем медиков
    val medicPersons = remember(persons) {
        persons.filter { it.rights.contains(Rights.Medic) }
    }

    // Определяем место работы из прав пациента
    val workplace = remember(person) {
        person.rights.firstOrNull { it != Rights.Any && it != Rights.Joker }
            ?.getDisplayName() ?: "Не указано"
    }

    // Состояния для полей
    var gender by remember { mutableStateOf(medicalRecord?.gender ?: "") }
    var dateOfBirthText by remember {
        mutableStateOf(
            medicalRecord?.dateOfBirth?.let { DateFormatter.formatEpochMillisTo1950Date(it) } ?: ""
        )
    }
    var doctor by remember { mutableStateOf(medicalRecord?.doctor ?: "") }
    var diagnosis by remember { mutableStateOf(person.health) }
    var prescribedTreatment by remember { mutableStateOf(medicalRecord?.prescribedTreatment ?: "") }
    var isSick by remember { mutableStateOf(person.health != "здоров") }
    var isHealthy by remember { mutableStateOf(person.health == "здоров") }

    // Состояния для выбора врача
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
                .widthIn(max = 700.dp)
                .heightIn(max = 800.dp)
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
                    .fillMaxHeight()
            ) {
                // ИСПРАВЛЕНО: Скроллируемая область с weight и padding внизу
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = "Мед. карта",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Пациент (только для отображения)
                    VengText(
                        text = "Пациент №${person.id}: ${person.firstName} ${person.lastName}",
                        color = dialogColors.borderLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Пол
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VengText(
                            text = "Пол:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Checkbox(
                            checked = gender == "M",
                            onCheckedChange = {
                                gender = if (it) "M" else ""
                                if (it) isHealthy = false
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
                                gender = if (it) "Ж" else ""
                                if (it) isHealthy = false
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

                    // Лечащий врач
                    Box {
                        val selectedDoctor = medicPersons.find { it.id == selectedDoctorId }
                        VengTextField(
                            value = if (isCustomDoctor) {
                                doctor
                            } else {
                                selectedDoctor?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                    ?: doctor.ifEmpty { "Выберите врача или введите ФИО..." }
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
                                .width(400.dp)
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

                    // Место работы (только для отображения)
                    VengText(
                        text = "Место работы: $workplace",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Диагноз
                    VengTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = "Диагноз",
                        placeholder = "Введите диагноз...",
                        modifier = Modifier.fillMaxWidth(),
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Назначенное лечение (многострочное поле)
                    VengTextField(
                        value = prescribedTreatment,
                        onValueChange = { prescribedTreatment = it },
                        label = "Назначенное лечение",
                        placeholder = "Введите назначенное лечение...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        theme = theme,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Статус
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VengText(
                            text = "Статус:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Checkbox(
                            checked = isSick,
                            onCheckedChange = {
                                isSick = it
                                isHealthy = !it
                                if (it) {
                                    if (diagnosis == "здоров") {
                                        diagnosis = ""
                                    }
                                } else {
                                    isHealthy = true
                                    diagnosis = "здоров"
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFF44336)
                            )
                        )
                        VengText(
                            text = "Болен",
                            color = Color(0xFFF44336),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Checkbox(
                            checked = isHealthy,
                            onCheckedChange = {
                                isHealthy = it
                                isSick = !it
                                if (it) {
                                    diagnosis = "здоров"
                                } else {
                                    isSick = true
                                    if (diagnosis == "здоров") {
                                        diagnosis = ""
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50)
                            )
                        )
                        VengText(
                            text = "Здоров",
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    if (medicalRecord != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        VengButton(
                            onClick = {
                                onDelete(medicalRecord.id)
                                onDismiss()
                            },
                            text = "Закрыть мед.карту",
                            modifier = Modifier.fillMaxWidth(),
                            padding = 12.dp,
                            theme = theme
                        )
                    }

                    // ИСПРАВЛЕНО: Добавляем Spacer внизу, чтобы последний элемент был виден
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ИСПРАВЛЕНО: Кнопки вне скроллируемой области, всегда видны
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(dialogColors.background)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Выйти",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            if (medicalRecord != null && validateInput(gender, dateOfBirthText, doctor, diagnosis)) {
                                val dateOfBirth = DateFormatter.formatDateTimeTo1950(dateOfBirthText)
                                val updatedRecord = medicalRecord.copy(
                                    firstName = person.firstName,
                                    lastName = person.lastName,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    doctor = doctor,
                                    prescribedTreatment = prescribedTreatment,
                                    workplace = workplace
                                )
                                onSave(medicalRecord.id, updatedRecord, diagnosis)
                            }
                        },
                        text = "Сохранить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = medicalRecord != null && validateInput(gender, dateOfBirthText, doctor, diagnosis),
                        theme = theme
                    )
                }
            }
        }
    }
}

private fun validateInput(
    gender: String,
    dateOfBirth: String,
    doctor: String,
    diagnosis: String
): Boolean {
    return gender.isNotBlank() &&
        dateOfBirth.isNotBlank() &&
        doctor.isNotBlank() &&
        diagnosis.isNotBlank()
}
