package org.vengeful.citymanager.screens.niis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.ROUTE_NIIS_CLEANING
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.composables.CallIndicator
import org.vengeful.citymanager.uikit.composables.EmergencyButton
import org.vengeful.citymanager.utilities.LocalTheme
import androidx.compose.ui.text.style.TextAlign
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.toRights
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.composables.personnel.PasswordDialog
import org.vengeful.citymanager.uikit.composables.personnel.PersonnelManagementDialog
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun NIISMainScreen(navController: NavController) {
    val currentTheme = LocalTheme
    val viewModel: NIISViewModel = koinViewModel()
    val severiteCounts by viewModel.severiteCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()

    var showSampleDialog by remember { mutableStateOf(false) }
    var sampleNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPersonnelManagementDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadSeveriteCounts()
        viewModel.loadAllPersons()
        viewModel.startStatusCheck()
        // Сбрасываем состояние "нажата" при входе на экран
        viewModel.resetEmergencyButtonState()
    }

    VengBackground(theme = currentTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VengText(
                    text = "НИИС",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                CallIndicator(
                    isCalled = callStatus?.isCalled ?: false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onDismiss = {
                        viewModel.resetCall()
                    }
                )


                VengText(
                    text = "Научно Исследовательский \n Институт Северита",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    lineHeight = 24.sp,
                    maxLines = 2
                )

                // Счётчики очищенного северита
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .background(
                            SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.5f),
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengText(
                        text = "Очищенный северит:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (isLoading) {
                        VengText(
                            text = "Загрузка...",
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        severiteCounts?.let { counts ->
                            VengText(
                                text = "Загрязнённый северит: ${counts.contaminated}",
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            VengText(
                                text = "Обычный северит: ${counts.normal}",
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            VengText(
                                text = "Кристально чистый северит: ${counts.crystalClear}",
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } ?: run {
                            VengText(
                                text = "Нет данных",
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Column {
                VengButton(
                    onClick = { showSampleDialog = true },
                    text = "Начать процесс очистки северита",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp)
                )

                VengButton(
                    onClick = { showPasswordDialog = true },
                    text = "Сотрудники",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 64.dp)
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = "Назад",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp)
                )
            }
        }

        // Тревожная кнопка в левом верхнем углу
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            EmergencyButton(
                onClick = { viewModel.sendEmergencyAlert() },
                modifier = Modifier,
                enabled = true
            )
            // Индикатор "нажата" под кнопкой
            if (viewModel.isEmergencyButtonPressed.collectAsState().value) {
                VengText(
                    text = "нажата",
                    color = Color(0xFFDC143C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
        }
    }

    // Диалог ввода номера образца
    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = {
                showSampleDialog = false
                sampleNumber = ""
                errorMessage = null
            },
            title = {
                VengText(
                    text = "Введите номер образца",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengTextField(
                        value = sampleNumber,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                sampleNumber = newValue
                                errorMessage = null
                            }
                        },
                        label = "Номер образца (6 цифр)",
                        placeholder = "000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        theme = currentTheme
                    )
                    if (errorMessage != null) {
                        VengText(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                VengButton(
                    onClick = {
                        if (sampleNumber.length == 6) {
                            navController.navigate("$ROUTE_NIIS_CLEANING/$sampleNumber")
                            showSampleDialog = false
                            sampleNumber = ""
                            errorMessage = null
                        } else {
                            errorMessage = "Номер должен состоять из 6 цифр"
                        }
                    },
                    text = "Начать очистку",
                    theme = currentTheme
                )
            },
            dismissButton = {
                VengButton(
                    onClick = {
                        showSampleDialog = false
                        sampleNumber = ""
                        errorMessage = null
                    },
                    text = "Отмена",
                    theme = currentTheme
                )
            },
            containerColor = SeveritepunkThemes.getColorScheme(currentTheme).background
        )
    }

    // Диалоги управления персоналом
    if (showPasswordDialog) {
        PasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordCorrect = {
                showPasswordDialog = false
                showPersonnelManagementDialog = true
            },
            theme = currentTheme
        )
    }

    if (showPersonnelManagementDialog) {
        val allPersons = viewModel.allPersons.collectAsState().value
        val personnel = viewModel.getPersonnelByRight(Enterprise.NIIS.toRights())
        PersonnelManagementDialog(
            enterpriseRight = Enterprise.NIIS.toRights(),
            allPersons = allPersons,
            personnel = personnel,
            isLoading = false,
            errorMessage = null,
            onAddPerson = { person ->
                viewModel.addRightToPerson(person.id, Enterprise.NIIS.toRights())
            },
            onRemovePerson = { person ->
                viewModel.removeRightFromPerson(person.id, Enterprise.NIIS.toRights())
            },
            onDismiss = { showPersonnelManagementDialog = false },
            theme = currentTheme
        )
    }
}
