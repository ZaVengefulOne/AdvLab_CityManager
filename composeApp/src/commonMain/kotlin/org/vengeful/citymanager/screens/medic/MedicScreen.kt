package org.vengeful.citymanager.screens.medic

import EditMedicalRecordDialog
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
import citymanager.composeapp.generated.resources.medic_name
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.ROUTE_MEDIC_ORDERS
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.medic.MedicalRecordDialog
import org.vengeful.citymanager.uikit.composables.medic.OrderMedicineDialog
import org.vengeful.citymanager.uikit.composables.medic.PatientCard
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme


@Composable
fun MedicScreen(navController: NavController) {
    val medicViewModel: MedicViewModel = koinViewModel()
    val patients by medicViewModel.patients.collectAsState()
    val isLoading by medicViewModel.isLoading.collectAsState()
    val medicines by medicViewModel.medicines.collectAsState()
    val availableAccounts by medicViewModel.availableAccounts.collectAsState()
    val currentPerson by medicViewModel.currentPerson.collectAsState()
    val currentMedicalRecord by medicViewModel.currentMedicalRecord.collectAsState()
    val medicalRecords by medicViewModel.medicalRecords.collectAsState()
    val errorMessage by medicViewModel.errorMessage.collectAsState()
    val successMessage by medicViewModel.successMessage.collectAsState()
    val allPersons by medicViewModel.allPersons.collectAsState()

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showMedicalRecordDialog by remember { mutableStateOf(false) }
    var showOrderMedicineDialog by remember { mutableStateOf(false) }
    var selectedPatientForEdit by remember { mutableStateOf<Person?>(null) }
    var patientSearchQuery by remember { mutableStateOf("") }

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

            // Центральная колонка - заголовок и список пациентов
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VengText(
                    text = stringResource(Res.string.medic_name),
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
                    value = patientSearchQuery,
                    onValueChange = { patientSearchQuery = it },
                    label = "Поиск пациентов",
                    placeholder = "Введите имя, фамилию или ID...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    theme = currentTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтрация пациентов
                val filteredPatients = remember(patients, patientSearchQuery) {
                    if (patientSearchQuery.isBlank()) {
                        patients
                    } else {
                        val searchText = patientSearchQuery.lowercase()
                        patients.filter { patient ->
                            "${patient.firstName} ${patient.lastName} ${patient.id}".lowercase().contains(searchText)
                        }
                    }
                }

                // Индикатор загрузки или список пациентов
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
                        items(filteredPatients) { patient ->
                            PatientCard(
                                modifier = Modifier.fillMaxWidth(),
                                person = patient,
                                medicalRecord = medicalRecords[patient.id],
                                theme = currentTheme,
                                onCardClick = { // НОВОЕ: Обработчик клика
                                    selectedPatientForEdit = patient
                                    medicViewModel.loadMedicalRecordByPersonId(patient.id)
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
                    onClick = { showMedicalRecordDialog = true },
                    text = "Создать медкарту",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                VengButton(
                    onClick = { showOrderMedicineDialog = true },
                    text = "Заказ лекарств",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                VengButton(
                    onClick = { navController.navigate(ROUTE_MEDIC_ORDERS) },
                    text = "Заказы",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
            }
        }

        // Диалог создания медкарты
        if (showMedicalRecordDialog) {
            MedicalRecordDialog(
                persons = allPersons,
                onDismiss = { showMedicalRecordDialog = false },
                onCreateRecord = { record, healthStatus ->
                    medicViewModel.createMedicalRecord(record, healthStatus)
                },
                theme = currentTheme
            )
        }

        if (showMedicalRecordDialog) {
            MedicalRecordDialog(
                persons = allPersons,
                onDismiss = { showMedicalRecordDialog = false },
                onCreateRecord = { record, healthStatus ->
                    medicViewModel.createMedicalRecord(record, healthStatus)
                },
                theme = currentTheme
            )
        }

        if (showOrderMedicineDialog) {
            OrderMedicineDialog(
                medicines = medicines,
                availableAccounts = availableAccounts,
                currentPerson = currentPerson,
                onDismiss = { showOrderMedicineDialog = false },
                onOrder = { medicineId, quantity, accountId ->
                    medicViewModel.orderMedicine(medicineId, quantity, accountId)
                },
                theme = currentTheme
            )
        }

        selectedPatientForEdit?.let { patient ->
            currentMedicalRecord?.let { record ->
                EditMedicalRecordDialog(
                    person = patient,
                    medicalRecord = record,
                    persons = allPersons,
                    onDismiss = {
                        selectedPatientForEdit = null
                        medicViewModel.clearCurrentMedicalRecord()
                    },
                    onSave = { recordId, updatedRecord, healthStatus ->
                        medicViewModel.updateMedicalRecord(recordId, updatedRecord, healthStatus)
                        selectedPatientForEdit = null
                    },
                    onDelete = { recordId ->
                        medicViewModel.deleteMedicalRecord(recordId)
                        selectedPatientForEdit = null
                    },
                    theme = currentTheme
                )
            }
        }
    }
}
