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
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import org.vengeful.citymanager.utils.bytesToImageBitmap
import org.vengeful.citymanager.data.police.FilePicker
import org.vengeful.citymanager.data.police.FingerprintsReader
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.DateFormatter

@Composable
fun PoliceRecordDialog(
    persons: List<Person>,
    fingerprintsReader: FingerprintsReader,
    filePicker: FilePicker,
    onDismiss: () -> Unit,
    onCreateRecord: (PoliceRecord, ByteArray?) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirthText by remember { mutableStateOf("") } // Формат: DD.MM.YYYY
    var workplace by remember { mutableStateOf("") }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }
    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var fingerprintNumber by remember { mutableStateOf<Int?>(null) }
    var isLoadingPhoto by remember { mutableStateOf(false) }
    var fingerprintImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var photoPreviewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Загружаем превью выбранного фото
    LaunchedEffect(selectedPhotoBytes) {
        photoPreviewBitmap = selectedPhotoBytes?.let { bytes ->
            try {
                bytesToImageBitmap(bytes)
            } catch (e: Exception) {
                println("Error creating photo preview: ${e.message}")
                null
            }
        }
    }

    // Загружаем изображение отпечатка при изменении номера
    LaunchedEffect(fingerprintNumber) {
        fingerprintImage = null
        if (fingerprintNumber != null) {
            try {
                val bytes = fingerprintsReader.loadFingerprintImage(fingerprintNumber!!)
                if (bytes != null) {
                    fingerprintImage = bytesToImageBitmap(bytes)
                }
            } catch (e: Exception) {
                println("Error loading fingerprint image: ${e.message}")
            }
        }
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
                        .padding(24.dp)
                ) {
                VengText(
                    text = "Личное дело",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Отображение фото (наверху)
                if (photoPreviewBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            bitmap = photoPreviewBitmap!!,
                            contentDescription = "Предпросмотр фото",
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, dialogColors.borderLight, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Выбор персоны
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
                                    workplace = person.rights.firstOrNull {
                                        it != Rights.Any && it != Rights.Joker
                                    }?.getDisplayName() ?: ""
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

                // ФИО
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

                // Номер отпечатка
                VengTextField(
                    value = fingerprintNumber?.toString() ?: "",
                    onValueChange = {
                        fingerprintNumber = it.toIntOrNull()
                    },
                    label = "Номер отпечатка",
                    placeholder = "Введите номер...",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Отображение отпечатка
                if (fingerprintNumber != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VengText(
                            text = "Отпечаток пальца",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(dialogColors.surface)
                                .border(2.dp, dialogColors.borderLight, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fingerprintImage != null) {
                                Image(
                                    bitmap = fingerprintImage!!,
                                    contentDescription = "Отпечаток пальца №$fingerprintNumber",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                VengText(
                                    text = "Отпечаток №$fingerprintNumber не найден",
                                    color = dialogColors.borderLight,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Загрузка фото
                VengButton(
                    onClick = {
                        isLoadingPhoto = true
                        scope.launch {
                            try {
                                selectedPhotoBytes = filePicker.pickImage()
                            } catch (e: Exception) {
                                println("Error picking image: ${e.message}")
                            } finally {
                                isLoadingPhoto = false
                            }
                        }
                    },
                    text = if (selectedPhotoBytes != null) "Фото выбрано" else "Выбрать фото",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme,
                    enabled = !isLoadingPhoto
                )

                }
                
                // Кнопки (вне скролла, всегда видны)
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
                            if (validateInput(firstName, lastName, dateOfBirthText, workplace) && selectedPersonId != null) {
                                val dateOfBirth = DateFormatter.formatDateTimeTo1950(dateOfBirthText)
                                val record = PoliceRecord(
                                    personId = selectedPersonId!!,
                                    firstName = firstName,
                                    lastName = lastName,
                                    dateOfBirth = dateOfBirth,
                                    workplace = workplace,
                                    fingerprintNumber = fingerprintNumber
                                )
                                onCreateRecord(record, selectedPhotoBytes)
                                onDismiss()
                            }
                        },
                        text = "Создать",
                        enabled = validateInput(firstName, lastName, dateOfBirthText, workplace) && selectedPersonId != null,
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
    firstName: String,
    lastName: String,
    dateOfBirth: String,
    workplace: String
): Boolean {
    return firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        dateOfBirth.isNotBlank() &&
        workplace.isNotBlank()
}

