package org.vengeful.citymanager.screens.court

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
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
import citymanager.composeapp.generated.resources.court_name
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.court.CreateHearingDialog
import org.vengeful.citymanager.uikit.composables.court.HearingCard
import org.vengeful.citymanager.uikit.composables.court.ViewHearingDialog
import org.vengeful.citymanager.uikit.composables.court.AppealHearingDialog
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.composables.court.HearingListDialog
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.composables.EmergencyButton
import org.vengeful.citymanager.uikit.composables.CallIndicator
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CourtScreen(navController: NavController) {
    val courtViewModel: CourtViewModel = koinViewModel()
    val casesSentToCourt by courtViewModel.casesSentToCourt.collectAsState()
    val hearings by courtViewModel.hearings.collectAsState()
    val isLoading by courtViewModel.isLoading.collectAsState()
    val errorMessage by courtViewModel.errorMessage.collectAsState()
    val successMessage by courtViewModel.successMessage.collectAsState()

    val currentHearing by courtViewModel.currentHearing.collectAsState()
    val callStatus by courtViewModel.callStatus.collectAsState()

    var currentTheme by remember { mutableStateOf(ColorTheme.SEVERITE) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showAppealDialog by remember { mutableStateOf(false) }
    var showAllHearings by remember { mutableStateOf(false) }
    var selectedHearingForView by remember { mutableStateOf<Hearing?>(null) }
    var hearingSearchQuery by remember { mutableStateOf("") }

    // Обновляем список слушаний при открытии экрана
    LaunchedEffect(Unit) {
        courtViewModel.loadAllHearings()
        courtViewModel.startStatusCheck()
        // Сбрасываем состояние "нажата" при входе на экран
        courtViewModel.resetEmergencyButtonState()
    }

    VengBackground(
        modifier = Modifier.fillMaxSize(),
        theme = currentTheme,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

                // Центральная колонка - заголовок и список слушаний
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VengText(
                        text = stringResource(Res.string.court_name),
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    CallIndicator(
                        isCalled = callStatus?.isCalled ?: false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onDismiss = {
                            courtViewModel.resetCall()
                        }
                    )


                    // Сообщения об ошибках/успехе
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Фильтрация слушаний
                    val filteredHearings = remember(hearings, hearingSearchQuery) {
                        if (hearingSearchQuery.isBlank()) {
                            hearings
                        } else {
                            val searchText = hearingSearchQuery.lowercase()
                            hearings.filter { hearing ->
                                "${hearing.id} ${hearing.caseId} ${hearing.plaintiffName} ${hearing.verdict} ${hearing.protocol}".lowercase().contains(searchText)
                            }
                        }
                    }

                    // Индикатор загрузки или список слушаний
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF4A90E2),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        if (filteredHearings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VengText(
                                    text = if (hearingSearchQuery.isBlank()) "Слушания отсутствуют" else "Слушания не найдены",
                                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredHearings) { hearing ->
                                    HearingCard(
                                        hearing = hearing,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            selectedHearingForView = hearing
                                            showViewDialog = true
                                        },
                                        theme = currentTheme
                                    )
                                }
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
                        onClick = { showCreateDialog = true },
                        text = "Новое",
                        theme = currentTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }

            // Кнопка "Показать все" внизу
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                VengButton(
                    onClick = { showAllHearings = true },
                    text = "Показать все",
                    theme = currentTheme,
                    modifier = Modifier.fillMaxWidth(0.3f)
                )
            }
        }

        // Тревожная кнопка в левом нижнем углу
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            EmergencyButton(
                onClick = { courtViewModel.sendEmergencyAlert() },
                modifier = Modifier,
                enabled = true
            )
            // Индикатор "нажата" под кнопкой
            if (courtViewModel.isEmergencyButtonPressed.collectAsState().value) {
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

    if (showCreateDialog) {
        CreateHearingDialog(
            cases = casesSentToCourt,
            onDismiss = { showCreateDialog = false },
            onCreateHearing = { hearing ->
                courtViewModel.createHearing(hearing)
                showCreateDialog = false
            },
            onUpdateCaseStatus = { caseId, status ->
                courtViewModel.updateCaseStatus(caseId, status)
            },
            theme = currentTheme
        )
    }

    // Обновляем список слушаний при открытии диалога "Показать все"
    LaunchedEffect(showAllHearings) {
        if (showAllHearings) {
            courtViewModel.loadAllHearings()
        }
    }

    if (showAllHearings) {
        HearingListDialog(
            hearings = hearings,
            onDismiss = { showAllHearings = false },
            onHearingClick = { hearing ->
                selectedHearingForView = hearing
                showAllHearings = false
                showViewDialog = true
            },
            theme = currentTheme
        )
    }

    if (showViewDialog && selectedHearingForView != null) {
        ViewHearingDialog(
            hearing = selectedHearingForView!!,
            onDismiss = {
                showViewDialog = false
                selectedHearingForView = null
            },
            onAppeal = { hearing ->
                // Return case status to SENT_TO_COURT when appeal is opened
                courtViewModel.updateCaseStatus(hearing.caseId, CaseStatus.SENT_TO_COURT)
                selectedHearingForView = hearing
                showViewDialog = false
                showAppealDialog = true
            },
            theme = currentTheme
        )
    }

    if (showAppealDialog && selectedHearingForView != null) {
        AppealHearingDialog(
            hearing = selectedHearingForView!!,
            onDismiss = {
                showAppealDialog = false
                selectedHearingForView = null
            },
            onUpdateHearing = { updatedHearing ->
                courtViewModel.updateHearing(updatedHearing.id, updatedHearing)
            },
            onUpdateCaseStatus = { caseId, status ->
                courtViewModel.updateCaseStatus(caseId, status)
            },
            theme = currentTheme
        )
    }
}
