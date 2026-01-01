package org.vengeful.citymanager.uikit.composables.police

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.vengeful.citymanager.utils.bytesToImageBitmap
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.police.FilePicker
import org.vengeful.citymanager.data.police.FingerprintsReader
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.news.AsyncNewsImage
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.DateFormatter

@Composable
fun EditPoliceRecordDialog(
    person: Person,
    policeRecord: PoliceRecord?,
    persons: List<Person>,
    fingerprintsReader: FingerprintsReader,
    filePicker: FilePicker,
    onDismiss: () -> Unit,
    onSave: (Int, PoliceRecord, ByteArray?) -> Unit,
    onDelete: (Int) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val scope = rememberCoroutineScope()

    // Определяем место работы из прав персоны
    val workplace = remember(person) {
        person.rights.firstOrNull { it != Rights.Any && it != Rights.Joker }
            ?.getDisplayName() ?: "Не указано"
    }

    // Состояния для полей
    var dateOfBirthText by remember {
        mutableStateOf(
            policeRecord?.dateOfBirth?.let { DateFormatter.formatEpochMillisToDate(it) } ?: ""
        )
    }
    var workplaceText by remember { mutableStateOf(policeRecord?.workplace ?: workplace) }
    var fingerprintNumber by remember { mutableStateOf(policeRecord?.fingerprintNumber?.toString() ?: "") }
    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoadingPhoto by remember { mutableStateOf(false) }
    var previewImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var fingerprintImage by remember { mutableStateOf<ImageBitmap?>(null) }

    // Загружаем изображение отпечатка при изменении номера
    LaunchedEffect(fingerprintNumber) {
        fingerprintImage = null
        val number = fingerprintNumber.toIntOrNull()
        if (number != null) {
            try {
                val bytes = fingerprintsReader.loadFingerprintImage(number)
                if (bytes != null) {
                    fingerprintImage = bytesToImageBitmap(bytes)
                }
            } catch (e: Exception) {
                println("Error loading fingerprint image: ${e.message}")
            }
        }
    }

    // Загружаем превью выбранного фото
    LaunchedEffect(selectedPhotoBytes) {
        previewImageBitmap = selectedPhotoBytes?.let { bytes ->
            try {
                bytesToImageBitmap(bytes)
            } catch (e: Exception) {
                println("Error creating preview: ${e.message}")
                null
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
                        .padding(horizontal = 24.dp, vertical = 24.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        when {
                            // Показываем превью нового выбранного фото
                            previewImageBitmap != null -> {
                                Image(
                                    bitmap = previewImageBitmap!!,
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
                            // Показываем существующее фото с сервера
                            policeRecord?.photoUrl != null -> {
                                val photoUrl = policeRecord.photoUrl
                                val fullImageUrl = if (photoUrl?.startsWith("http") ?: false) {
                                    photoUrl
                                } else {
                                    "$SERVER_BASE_URL$photoUrl"
                                }
                                AsyncNewsImage(
                                    imageUrl = fullImageUrl,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .aspectRatio(1f)
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, dialogColors.borderLight, RoundedCornerShape(8.dp))
                                )
                            }
                            // Нет фото
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .aspectRatio(1f)
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(dialogColors.surface)
                                        .border(2.dp, dialogColors.borderDark, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    VengText(
                                        text = "Нет фото",
                                        color = dialogColors.borderLight,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Персона (только для отображения)
                    VengText(
                        text = "Житель №${person.id}: ${person.firstName} ${person.lastName}",
                        color = dialogColors.borderLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Дата рождения
                    VengTextField(
                        value = dateOfBirthText,
                        onValueChange = { dateOfBirthText = it },
                        label = "Дата рождения",
                        placeholder = "DD.MM.YYYY",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        theme = theme
                    )

                    // Место работы
                    VengTextField(
                        value = workplaceText,
                        onValueChange = { workplaceText = it },
                        label = "Место работы",
                        placeholder = "Введите место работы...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        theme = theme
                    )

                    // Номер отпечатка
                    VengTextField(
                        value = fingerprintNumber,
                        onValueChange = { fingerprintNumber = it },
                        label = "Номер отпечатка",
                        placeholder = "Введите номер...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        theme = theme
                    )

                    // Отображение отпечатка
                    if (fingerprintNumber.isNotBlank() && fingerprintNumber.toIntOrNull() != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
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
                        text = when {
                            previewImageBitmap != null -> "Заменить фото"
                            policeRecord?.photoUrl != null -> "Заменить фото"
                            else -> "Выбрать фото"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        theme = theme,
                        enabled = !isLoadingPhoto
                    )

                    // Возраст (вычисляемый)
                    policeRecord?.let { record ->
                        VengText(
                            text = "Возраст: ${record.age}",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(dialogColors.background)
                ) {
                    VengButton(
                        onClick = {
                            if (policeRecord != null) {
                                onDelete(policeRecord.id)
                            }
                        },
                        text = "Удалить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = onDismiss,
                        text = "Выйти",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            if (policeRecord != null && validateInput(dateOfBirthText, workplaceText)) {
                                val dateOfBirth = DateFormatter.formatDateTimeTo1950(dateOfBirthText)
                                val updatedRecord = policeRecord.copy(
                                    firstName = person.firstName,
                                    lastName = person.lastName,
                                    dateOfBirth = dateOfBirth,
                                    workplace = workplaceText,
                                    fingerprintNumber = fingerprintNumber.toIntOrNull()
                                )
                                onSave(policeRecord.id, updatedRecord, selectedPhotoBytes)
                            }
                        },
                        text = "Сохранить",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = policeRecord != null && validateInput(dateOfBirthText, workplaceText),
                        theme = theme
                    )
                }
            }
        }
    }
}

private fun validateInput(
    dateOfBirth: String,
    workplace: String
): Boolean {
    return dateOfBirth.isNotBlank() && workplace.isNotBlank()
}

