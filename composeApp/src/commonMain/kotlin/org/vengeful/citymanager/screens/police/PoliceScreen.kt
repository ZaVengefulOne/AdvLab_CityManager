package org.vengeful.citymanager.screens.police

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.di.koinViewModel
import org.koin.core.context.GlobalContext
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.police.CaseCard
import org.vengeful.citymanager.uikit.composables.police.CaseDialog
import org.vengeful.citymanager.uikit.composables.police.CaseList
import org.vengeful.citymanager.uikit.composables.police.EditCaseDialog
import org.vengeful.citymanager.uikit.composables.police.ViewCaseDialog
import org.vengeful.citymanager.uikit.composables.veng.VengTabRow
import org.vengeful.citymanager.uikit.composables.police.EditPoliceRecordDialog
import org.vengeful.citymanager.uikit.composables.police.PoliceRecordCard
import org.vengeful.citymanager.uikit.composables.police.PoliceRecordDialog
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.composables.CallIndicator
import org.vengeful.citymanager.utilities.LocalTheme
import kotlinx.coroutines.launch
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.toRights
import org.vengeful.citymanager.uikit.composables.personnel.PasswordDialog
import org.vengeful.citymanager.uikit.composables.personnel.PersonnelManagementDialog
import androidx.compose.runtime.collectAsState

@Composable
fun PoliceScreen(navController: NavController) {
    val policeViewModel: PoliceViewModel = koinViewModel()
    val caseViewModel: CaseViewModel = koinViewModel()
    val userInteractor: IUserInteractor = remember { GlobalContext.get().get() }
    val personInteractor: IPersonInteractor = remember { GlobalContext.get().get() }

    val persons by policeViewModel.persons.collectAsState()
    val isLoading by policeViewModel.isLoading.collectAsState()
    val currentRecord by policeViewModel.currentRecord.collectAsState()
    val policeRecords by policeViewModel.policeRecords.collectAsState()
    val errorMessage by policeViewModel.errorMessage.collectAsState()
    val successMessage by policeViewModel.successMessage.collectAsState()
    val allPersons by policeViewModel.allPersons.collectAsState()

    val cases by caseViewModel.cases.collectAsState()
    val casesLoading by caseViewModel.isLoading.collectAsState()
    val casesError by caseViewModel.errorMessage.collectAsState()
    val casesSuccess by caseViewModel.successMessage.collectAsState()
    val callStatus by policeViewModel.callStatus.collectAsState()
    val emergencyAlerts by policeViewModel.emergencyAlerts.collectAsState()

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Личные дела, 1 = Дела
    var showRecordDialog by remember { mutableStateOf(false) }
    var showCaseDialog by remember { mutableStateOf(false) }
    var showFingerprintsBrowser by remember { mutableStateOf(false) }
    var selectedPersonForEdit by remember { mutableStateOf<Person?>(null) }
    var selectedCase by remember { mutableStateOf<Case?>(null) }
    var personSearchQuery by remember { mutableStateOf("") }
    var caseSearchQuery by remember { mutableStateOf("") }

    var investigatorPersonId by remember { mutableIntStateOf(-1) }
    var investigatorName by remember { mutableStateOf("Неизвестно") }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPersonnelManagementDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val filePicker = remember { createFilePicker() }
    val fingerprintsReader = remember { createFingerprintsReader() }

    // Загружаем информацию о текущем пользователе
    LaunchedEffect(Unit) {
        scope.launch {
            val currentUser = userInteractor.getCurrentUserWithPersonId()
            if (currentUser != null && currentUser.personId > 0) {
                investigatorPersonId = currentUser.personId
                val person = personInteractor.getPersonById(currentUser.personId)
                investigatorName = person?.let { "${it.firstName} ${it.lastName}" } ?: "Неизвестно"
            }
        }
        policeViewModel.startStatusCheck()
        policeViewModel.startAlertsCheck()
    }

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

                VengButton(
                    onClick = { showPasswordDialog = true },
                    text = "Сотрудники",
                    theme = currentTheme,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                )
            }

            // Центральная колонка - заголовок и список личных дел/дел
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

                CallIndicator(
                    isCalled = callStatus?.isCalled ?: false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onDismiss = {
                        policeViewModel.resetCall()
                    }
                )

                // Тревожные уведомления
                if (emergencyAlerts.isNotEmpty()) {
                    println("PoliceScreen: Displaying ${emergencyAlerts.size} emergency alerts")
                }
                emergencyAlerts.forEachIndexed { index, alert ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                Color(0xFFDC143C),
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .border(
                                2.dp,
                                Color(0xFFFF0000),
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                VengText(
                                    text = "ТРЕВОГА!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                VengText(
                                    text = "Предприятие: ${alert.enterprise.getDisplayName()}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                val timeAgo = (System.currentTimeMillis() - alert.timestamp) / 1000
                                val timeText = when {
                                    timeAgo < 60 -> "$timeAgo сек назад"
                                    timeAgo < 3600 -> "${timeAgo / 60} мин назад"
                                    else -> "${timeAgo / 3600} ч назад"
                                }
                                VengText(
                                    text = timeText,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            VengButton(
                                onClick = { policeViewModel.dismissEmergencyAlert(index) },
                                text = "Закрыть",
                                modifier = Modifier.height(40.dp),
                                padding = 8.dp,
                                theme = currentTheme
                            )
                        }
                    }
                }

                // Вкладки
                VengTabRow(
                    selectedTabIndex = selectedTab,
                    tabs = listOf("Личные дела", "Дела"),
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth(),
                    theme = currentTheme
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Сообщения об ошибках/успехе
                (if (selectedTab == 0) errorMessage else casesError)?.let {
                    VengText(
                        text = it,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                (if (selectedTab == 0) successMessage else casesSuccess)?.let {
                    VengText(
                        text = it,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Поле поиска для личных дел
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
                    1 -> {
                        // Поле поиска для дел
                        VengTextField(
                            value = caseSearchQuery,
                            onValueChange = { caseSearchQuery = it },
                            label = "Поиск дел",
                            placeholder = "Введите номер дела, имя заявителя или подозреваемого...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            theme = currentTheme
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Разделение дел на активные и закрытые
                        val activeCases = remember(cases) {
                            cases.filter { it.status != CaseStatus.CLOSED }
                        }
                        val closedCases = remember(cases) {
                            cases.filter { it.status == CaseStatus.CLOSED }
                        }

                        // Фильтрация активных дел
                        val filteredActiveCases = remember(activeCases, caseSearchQuery) {
                            if (caseSearchQuery.isBlank()) {
                                activeCases
                            } else {
                                val searchText = caseSearchQuery.lowercase()
                                activeCases.filter { case ->
                                    "${case.id} ${case.complainantName} ${case.suspectName} ${case.violationArticle}".lowercase().contains(searchText)
                                }
                            }
                        }

                        // Фильтрация закрытых дел
                        val filteredClosedCases = remember(closedCases, caseSearchQuery) {
                            if (caseSearchQuery.isBlank()) {
                                closedCases
                            } else {
                                val searchText = caseSearchQuery.lowercase()
                                closedCases.filter { case ->
                                    "${case.id} ${case.complainantName} ${case.suspectName} ${case.violationArticle}".lowercase().contains(searchText)
                                }
                            }
                        }

                        // Индикатор загрузки или список дел
                        if (casesLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF4A90E2),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            if (filteredActiveCases.isEmpty() && filteredClosedCases.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    VengText(
                                        text = "Дела не найдены",
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
                                    // Активные дела
                                    if (filteredActiveCases.isNotEmpty()) {
                                        item {
                                            VengText(
                                                text = "Активные дела",
                                                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                            )
                                        }
                                        items(filteredActiveCases) { case ->
                                            CaseCard(
                                                case = case,
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = { selectedCase = case },
                                                theme = currentTheme
                                            )
                                        }
                                    }

                                    // Архив (закрытые дела)
                                    if (filteredClosedCases.isNotEmpty()) {
                                        item {
                                            VengText(
                                                text = "Архив",
                                                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                            )
                                        }
                                        items(filteredClosedCases) { case ->
                                            CaseCard(
                                                case = case,
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = { selectedCase = case },
                                                theme = currentTheme
                                            )
                                        }
                                    }
                                }
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
                if (selectedTab == 0) {
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
                } else {
                    VengButton(
                        onClick = {
                            if (investigatorPersonId > 0) {
                                showCaseDialog = true
                            }
                        },
                        text = "Создать дело",
                        theme = currentTheme,
                        enabled = investigatorPersonId > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    )
                }
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

        // Диалог создания дела
        if (showCaseDialog && investigatorPersonId > 0) {
            CaseDialog(
                persons = allPersons,
                investigatorPersonId = investigatorPersonId,
                investigatorName = investigatorName,
                filePicker = filePicker,
                onDismiss = { showCaseDialog = false },
                onCreateCase = { case, photoBytes ->
                    caseViewModel.createCase(case, photoBytes)
                },
                theme = currentTheme
            )
        }

        // Диалог редактирования/просмотра дела
        selectedCase?.let { case ->
            if (case.status == CaseStatus.CLOSED) {
                // Просмотр закрытого дела (read-only)
                ViewCaseDialog(
                    case = case,
                    onDismiss = {
                        selectedCase = null
                    },
                    onDelete = { caseId ->
                        caseViewModel.deleteCase(caseId)
                        selectedCase = null
                    },
                    theme = currentTheme
                )
            } else {
                // Редактирование активного дела
                EditCaseDialog(
                    case = case,
                    persons = allPersons,
                    filePicker = filePicker,
                    onDismiss = {
                        selectedCase = null
                    },
                    onSave = { updatedCase, photoBytes ->
                        caseViewModel.updateCase(case.id, updatedCase, photoBytes)
                        selectedCase = null
                    },
                    onSendToCourt = { updatedCase, photoBytes ->
                        caseViewModel.updateCase(case.id, updatedCase.copy(status = CaseStatus.SENT_TO_COURT), photoBytes)
                        selectedCase = null
                    },
                    onClose = { caseId ->
                        caseViewModel.updateCaseStatus(caseId, CaseStatus.CLOSED)
                        selectedCase = null
                    },
                    theme = currentTheme
                )
            }
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
            val personnel = policeViewModel.getPersonnelByRight(Enterprise.POLICE.toRights())
            PersonnelManagementDialog(
                enterpriseRight = Enterprise.POLICE.toRights(),
                allPersons = allPersons,
                personnel = personnel,
                isLoading = false,
                errorMessage = policeViewModel.errorMessage.collectAsState().value,
                onAddPerson = { person ->
                    policeViewModel.addRightToPerson(person.id, Enterprise.POLICE.toRights())
                },
                onRemovePerson = { person ->
                    policeViewModel.removeRightFromPerson(person.id, Enterprise.POLICE.toRights())
                },
                onDismiss = { showPersonnelManagementDialog = false },
                theme = currentTheme
            )
        }
    }
}
