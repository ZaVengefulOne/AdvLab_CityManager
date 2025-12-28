package org.vengeful.citymanager.screens.police

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.back
import citymanager.composeapp.generated.resources.police_name
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.data.police.FilePicker
import org.vengeful.citymanager.data.police.FingerprintsReader
import org.vengeful.citymanager.data.police.createFilePicker
import org.vengeful.citymanager.data.police.createFingerprintsReader
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.police.EditPoliceRecordDialog
import org.vengeful.citymanager.uikit.composables.police.PoliceRecordCard
import org.vengeful.citymanager.uikit.composables.police.PoliceRecordDialog
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun PoliceScreen(navController: NavController) {
    val policeViewModel: PoliceViewModel = koinViewModel()
    val persons by policeViewModel.persons.collectAsState()
    val isLoading by policeViewModel.isLoading.collectAsState()
    val currentRecord by policeViewModel.currentRecord.collectAsState()
    val policeRecords by policeViewModel.policeRecords.collectAsState()
    val errorMessage by policeViewModel.errorMessage.collectAsState()
    val successMessage by policeViewModel.successMessage.collectAsState()
    val allPersons by policeViewModel.allPersons.collectAsState()

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var showFingerprintsBrowser by remember { mutableStateOf(false) }
    var selectedPersonForEdit by remember { mutableStateOf<Person?>(null) }
    var personSearchQuery by remember { mutableStateOf("") }

    val filePicker = remember { createFilePicker() }
    val fingerprintsReader = remember { createFingerprintsReader() }

    VengBackground(
        modifier = Modifier.fillMaxSize(),
        theme = currentTheme,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Левая колонка - кнопки навигации
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                VengButton(
                    onClick = { navController.popBackStack() },
                    text = stringResource(Res.string.back),
                    theme = currentTheme,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                )

                ThemeSwitcher(
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                )
            }

            // Центральная колонка - заголовок и список личных дел
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VengText(
                    text = stringResource(Res.string.police_name),
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                errorMessage?.let {
                    VengText(
                        text = it,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                successMessage?.let {
                    VengText(
                        text = it,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Поле поиска
                VengTextField(
                    value = personSearchQuery,
                    onValueChange = { personSearchQuery = it },
                    label = "Поиск личных дел",
                    placeholder = "Введите имя, фамилию или ID...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    theme = currentTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтрация персон
                val filteredPersons = remember(persons, personSearchQuery) {
                    if (personSearchQuery.isBlank()) {
                        persons
                    } else {
                        val searchText = personSearchQuery.lowercase()
                        persons.filter { person ->
                            "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                        }
                    }
                }

                // Индикатор загрузки или список личных дел
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF4A90E2),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(filteredPersons) { person ->
                            PoliceRecordCard(
                                modifier = Modifier.fillMaxWidth(),
                                person = person,
                                policeRecord = policeRecords[person.id],
                                theme = currentTheme,
                                onCardClick = {
                                    selectedPersonForEdit = person
                                    policeViewModel.loadPoliceRecordByPersonId(person.id)
                                }
                            )
                        }
                    }
                }
            }

            // Правая колонка - кнопки действий
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                VengButton(
                    onClick = { showRecordDialog = true },
                    text = "Создать личное дело",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                VengButton(
                    onClick = { showFingerprintsBrowser = true },
                    text = "Просмотр отпечатков",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
            }
        }

        // Диалог создания личного дела
        if (showRecordDialog) {
            PoliceRecordDialog(
                persons = allPersons,
                fingerprintsReader = fingerprintsReader,
                filePicker = filePicker,
                onDismiss = { showRecordDialog = false },
                onCreateRecord = { record, photoBytes ->
                    policeViewModel.createPoliceRecord(record, photoBytes)
                },
                theme = currentTheme
            )
        }

        selectedPersonForEdit?.let { person ->
            currentRecord?.let { record ->
                EditPoliceRecordDialog(
                    person = person,
                    policeRecord = record,
                    persons = allPersons,
                    fingerprintsReader = fingerprintsReader,
                    filePicker = filePicker,
                    onDismiss = {
                        selectedPersonForEdit = null
                        policeViewModel.clearCurrentRecord()
                    },
                    onSave = { recordId, updatedRecord, photoBytes ->
                        policeViewModel.updatePoliceRecord(recordId, updatedRecord, photoBytes)
                        selectedPersonForEdit = null
                    },
                    onDelete = { recordId ->
                        policeViewModel.deletePoliceRecord(recordId)
                        selectedPersonForEdit = null
                    },
                    theme = currentTheme
                )
            }
        }

        if (showFingerprintsBrowser) {
            FingerprintsBrowser(
                fingerprintsReader = fingerprintsReader,
                policeViewModel = policeViewModel,
                onDismiss = { showFingerprintsBrowser = false },
                onRecordSelected = { person ->
                    selectedPersonForEdit = person
                    showFingerprintsBrowser = false
                },
                theme = currentTheme
            )
        }
    }
}
