package org.vengeful.citymanager.screens.administration

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.audio.rememberSoundPlayer
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.administration.AdminChatWidget
import org.vengeful.citymanager.uikit.composables.administration.ControlLossIndicator
import org.vengeful.citymanager.uikit.composables.administration.EmergencyShutdownDialog
import org.vengeful.citymanager.uikit.composables.administration.EnterpriseCallWidget
import org.vengeful.citymanager.uikit.composables.administration.SeveriteRateGraph
import org.vengeful.citymanager.uikit.composables.administration.SeveriteSalesWidget
import org.vengeful.citymanager.uikit.composables.EmergencyButton
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.dialogs.RegisterDialog
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.person.*
import org.vengeful.citymanager.uikit.composables.user.UserEditDialog
import org.vengeful.citymanager.uikit.composables.user.UserList
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import androidx.compose.runtime.collectAsState
import org.vengeful.citymanager.models.emergencyShutdown.ErrorResponse
import org.vengeful.citymanager.ROUTE_USERS_AND_PERSONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrationScreen(navController: NavController) {
    val administrationViewModel: AdministrationViewModel = koinViewModel()
    val persons = administrationViewModel.persons.collectAsState().value
    val users = administrationViewModel.users.collectAsState().value
    val severitRate = administrationViewModel.severitRate.collectAsState().value
    val controlLossThreshold = administrationViewModel.controlLossThreshold.collectAsState().value
    val severitRateHistory = administrationViewModel.severitRateHistory.collectAsState().value
    val chatMessages = administrationViewModel.chatMessages.collectAsState().value
    val isEmergencyShutdownActive = administrationViewModel.isEmergencyShutdownActive.collectAsState().value
    val remainingTimeSeconds = administrationViewModel.remainingTimeSeconds.collectAsState().value
    val severites = administrationViewModel.severites.collectAsState().value


    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEmergencyShutdownDialog by remember { mutableStateOf(false) }
    var emergencyShutdownError by remember { mutableStateOf<String?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var personToEdit by remember { mutableStateOf<Person?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    var showShutdownAnimation by remember { mutableStateOf(false) }
    var showRestartAnimation by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val soundPlayer = rememberSoundPlayer()

    val getId = remember { mutableStateOf("") }
    val delId = remember { mutableStateOf("") }

    val bigSpacer = 120.dp
    val defaultSpacer = 24.dp
    val bigPadding = 16.dp
    val mediumPadding = 12.dp
    val defaultPadding = 8.dp

    val dividerColor = Color(0xFFD4AF37)
    val transColor = Color.Transparent

    val rotationAngle by animateFloatAsState(
        targetValue = if (isRefreshing) 720f else 0f,
        animationSpec = if (isRefreshing) {
            tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "refresh_rotation"
    )

    if (showShutdownAnimation) {
        ShutdownAnimation(
            onComplete = { navController.popBackStack() },
            theme = currentTheme,
            soundPlayer = soundPlayer
        )
        return
    }

    if (showRestartAnimation) {
        RestartAnimation(
            onComplete = { showRestartAnimation = false },
            theme = currentTheme,
            soundPlayer = soundPlayer
        )
        return
    }

    LaunchedEffect(Unit) {
        administrationViewModel.getPersons()
        administrationViewModel.getUsers()
        administrationViewModel.getAdminConfig()
        administrationViewModel.startConfigUpdates()
        administrationViewModel.checkEmergencyShutdownStatus()
        administrationViewModel.loadSeverites()
        administrationViewModel.resetEmergencyButtonState()
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            delay(1000)
            isRefreshing = false
        }
    }


    VengBackground(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        theme = currentTheme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(defaultSpacer),
            verticalArrangement = Arrangement.spacedBy(defaultSpacer),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(defaultSpacer),
            ) {
                VengButton(
                    onClick = {
                        isRefreshing = true
                        refreshTrigger++
                        administrationViewModel.getPersons()
                        administrationViewModel.getUsers()
                    },
                    modifier = Modifier.weight(0.1f),
                    theme = currentTheme,
                    isIconButton = true,
                    content = {
                        Icon(
                            painter = painterResource(Res.drawable.refresh),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    rotationZ = rotationAngle
                                }
                        )
                    }
                )

                ThemeSwitcher(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        LocalTheme = newTheme
                        currentTheme = LocalTheme
                    },
                    modifier = Modifier
                        .weight(0.1f),
                )

                VengText(
                    text = stringResource(Res.string.administration_title),
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .padding(bottom = defaultPadding)
                        .weight(0.7f)
                )

                VengButton(
                    text = stringResource(Res.string.back),
                    onClick = {
                        navController.popBackStack()
                    },
                    theme = currentTheme,
                    modifier = Modifier
                        .padding(bottom = defaultPadding)
                        .weight(0.1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(defaultSpacer),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(defaultSpacer),
                    horizontalAlignment = Alignment.Start
                ) {
                    SeveriteRateGraph(
                        currentRate = severitRate,
                        history = severitRateHistory,
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .wrapContentHeight(),
                        graphColor = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f)
                    )

                    ControlLossIndicator(
                        threshold = controlLossThreshold,
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .wrapContentHeight(),
                        backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VengButton(
                            onClick = {
                                emergencyShutdownError = null
                                showEmergencyShutdownDialog = true
                            },
                            text = "Экстренное отключение",
                            modifier = Modifier.fillMaxWidth(),
                            theme = currentTheme,
                            enabled = !isEmergencyShutdownActive,
                            padding = 16.dp
                        )

                        // Текст с информацией о блокировке
                        if (isEmergencyShutdownActive && remainingTimeSeconds != null) {
                            val minutes = remainingTimeSeconds / 60
                            val seconds = remainingTimeSeconds % 60
                            val timeText = if (minutes > 0) {
                                "$minutes мин ${seconds}с"
                            } else {
                                "${seconds}с"
                            }

                            VengText(
                                text = "Блокировка активна. Осталось: $timeText",
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        } else if (isEmergencyShutdownActive) {
                            VengText(
                                text = "Блокировка активна",
                                color = Color(0xFFE74C3C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        VengButton(
                            onClick = {
                                administrationViewModel.downloadGameBackup("html")
                            },
                            text = "Выгрузить бэкап",
                            modifier = Modifier.fillMaxWidth(),
                            theme = LocalTheme,
                            enabled = true
                        )

                        VengButton(
                            onClick = {
                                administrationViewModel.downloadLimitedMasterBackup()
                            },
                            text = "Выгрузить служебный бэкап",
                            modifier = Modifier.fillMaxWidth(),
                            theme = LocalTheme,
                            enabled = true
                        )

                        VengButton(
                            onClick = {
                                administrationViewModel.uploadLimitedMasterBackup()
                            },
                            text = "Загрузить служебный бэкап",
                            modifier = Modifier.fillMaxWidth(),
                            theme = LocalTheme,
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmergencyButton(
                                onClick = { administrationViewModel.sendEmergencyAlert() },
                                modifier = Modifier,
                                enabled = true
                            )
                        }

                        // Индикатор "нажата" под кнопкой
                        if (administrationViewModel.isEmergencyButtonPressed.collectAsState().value) {
                            VengText(
                                text = "нажата",
                                color = Color(0xFFDC143C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showEmergencyShutdownDialog) {
                            EmergencyShutdownDialog(
                                onDismiss = {
                                    showEmergencyShutdownDialog = false
                                    emergencyShutdownError = null
                                    // Очищаем ошибку в ViewModel при закрытии диалога
                                    administrationViewModel.clearErrorMessage()
                                },
                                onConfirm = { durationMinutes, password ->
                                    emergencyShutdownError = null // Сбрасываем ошибку при новой попытке
                                    administrationViewModel.activateEmergencyShutdown(durationMinutes, password)
                                },
                                theme = currentTheme,
                                errorMessage = emergencyShutdownError
                            )

                            val errorMessageState = administrationViewModel.errorMessage.collectAsState()
                            LaunchedEffect(errorMessageState.value) {
                                val error = errorMessageState.value
                                if (error != null) {
                                    if (error.contains("password", ignoreCase = true) ||
                                        error.contains("пароль", ignoreCase = true) ||
                                        error.contains("Invalid emergency shutdown", ignoreCase = true) ||
                                        error.contains("Неверный пароль", ignoreCase = true)) {
                                        emergencyShutdownError = error
                                    } else if (error.contains("Failed to activate", ignoreCase = true)) {
                                        emergencyShutdownError = error
                                    }
                                }
                            }

                            // Закрываем диалог только при успехе
                            LaunchedEffect(isEmergencyShutdownActive) {
                                if (isEmergencyShutdownActive) {
                                    showEmergencyShutdownDialog = false
                                    emergencyShutdownError = null
                                    administrationViewModel.clearErrorMessage()
                                }
                            }
                        }
                    }
                }
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(defaultSpacer),
                        horizontalAlignment = Alignment.End
                    ) {
                        AdminChatWidget(
                            messages = chatMessages,
                            onSendMessage = { text ->
                                administrationViewModel.sendChatMessage(text)
                            },
                            modifier = Modifier
                                .wrapContentHeight(),
                            backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f),
                            borderColor = SeveritepunkThemes.getColorScheme(currentTheme).borderLight
                        )

                        EnterpriseCallWidget(
                            onCallEnterprise = { enterprise ->
                                administrationViewModel.callEnterprise(enterprise)
                            },
                            modifier = Modifier
                                .wrapContentHeight(),
                            backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f),
                            borderColor = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            theme = currentTheme
                        )

                        SeveriteSalesWidget(
                            severites = severites,
                            severitRate = severitRate,
                            onSell = { severiteIds ->
                                administrationViewModel.sellSeverite(severiteIds)
                            },
                            modifier = Modifier
                                .wrapContentHeight(),
                            backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f),
                            borderColor = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            theme = currentTheme
                        )
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = defaultSpacer),
                    horizontalArrangement = Arrangement.spacedBy(defaultSpacer)
                ) {
                    // Кнопка для управления пользователями и жителями
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(mediumPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VengText(
                            text = "Управление пользователями и жителями",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = mediumPadding),
                            textAlign = TextAlign.Center
                        )
                        VengButton(
                            onClick = {
                                navController.navigate(ROUTE_USERS_AND_PERSONS)
                            },
                            text = "Открыть списки",
                            modifier = Modifier.fillMaxWidth(),
                            padding = 16.dp,
                            theme = currentTheme
                        )
                        VengText(
                            text = "Пользователей: ${users.size}\nЖителей: ${persons.size}",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = mediumPadding),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    transColor,
                                    dividerColor,
                                    transColor,
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(mediumPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = stringResource(Res.string.person_list, persons.size),
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f)
                        )
                        VengButton(
                            onClick = { showAddDialog = true },
                            text = stringResource(Res.string.add_new_person),
                            modifier = Modifier.weight(0.1f),
                            theme = currentTheme,
                        )
                    }

                    if (persons.isNotEmpty()) {
                        PersonsGrid(
                            persons = persons,
                            modifier = Modifier.fillMaxSize(),
                            onPersonClick = { person ->
                                selectedPerson = person
                            },
                            theme = currentTheme,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.2f)
                                )
                                .padding(defaultSpacer),
                            contentAlignment = Alignment.Center
                        ) {
                            VengText(
                                text = stringResource(Res.string.base_empty),
                                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            if (showAddDialog) {
                PersonDialog(
                    onDismiss = { showAddDialog = false },
                    onAddPerson = { person ->
                        administrationViewModel.addPerson(person)
                        administrationViewModel.getPersons()
                    },
                    theme = currentTheme,
                )
            }

            selectedPerson?.let { person ->
                PersonDetailedDialog(
                    person = person,
                    onDismiss = { selectedPerson = null },
                    theme = currentTheme,
                )
            }

            // Диалог редактирования пользователя
            userToEdit?.let { user ->
                UserEditDialog(
                    user = user,
                    persons = persons,
                    onDismiss = { userToEdit = null },
                    onSave = { updatedUser, password, personId ->
                        administrationViewModel.updateUser(updatedUser, password, personId)
                        userToEdit = null
                    },
                    theme = currentTheme
                )
            }

            // Диалог редактирования жителя
            personToEdit?.let { person ->
                PersonEditDialog(
                    person = person,
                    onDismiss = { personToEdit = null },
                    onSave = { updatedPerson ->
                        administrationViewModel.updatePerson(updatedPerson)
                        personToEdit = null
                    },
                    theme = currentTheme
                )
            }

            // Диалог подтверждения удаления пользователя
            userToDelete?.let { user ->
                DeleteConfirmationDialog(
                    onDismiss = { userToDelete = null },
                    onConfirm = {
                        administrationViewModel.deleteUser(user.id)
                        userToDelete = null
                    },
                    theme = currentTheme
                )
            }

            // Диалог подтверждения удаления жителя
            personToDelete?.let { person ->
                DeleteConfirmationDialog(
                    onDismiss = { personToDelete = null },
                    onConfirm = {
                        administrationViewModel.deletePerson(person.id)
                        personToDelete = null
                    },
                    theme = currentTheme
                )
            }

            if (showRegisterDialog) {
                val registerState = administrationViewModel.registerState.collectAsState().value
                RegisterDialog(
                    onDismiss = {
                        showRegisterDialog = false
                        administrationViewModel.resetRegisterState()
                    },
                    onRegister = { username, password, personId ->
                        administrationViewModel.register(username, password, personId)
                    },
                    persons = persons,
                    isLoading = registerState is RegisterUiState.Loading,
                    errorMessage = when (registerState) {
                        is RegisterUiState.Error -> registerState.message
                        else -> null
                    },
                    theme = currentTheme
                )

                LaunchedEffect(registerState) {
                    if (registerState is RegisterUiState.Success) {
                        showRegisterDialog = false
                        administrationViewModel.resetRegisterState()
                    }
                }
            }

            if (showEmergencyShutdownDialog) {
                EmergencyShutdownDialog(
                    onDismiss = {
                        showEmergencyShutdownDialog = false
                        emergencyShutdownError = null
                        administrationViewModel.clearErrorMessage()
                    },
                    onConfirm = { durationMinutes, password ->
                        emergencyShutdownError = null
                        administrationViewModel.activateEmergencyShutdown(durationMinutes, password)
                    },
                    theme = currentTheme,
                    errorMessage = emergencyShutdownError
                )

                // Обработка ошибок из ViewModel
                val errorMessageState = administrationViewModel.errorMessage.collectAsState()
                LaunchedEffect(errorMessageState.value) {
                    val error = errorMessageState.value
                    if (error != null) {
                        if (error.contains("password", ignoreCase = true) ||
                            error.contains("пароль", ignoreCase = true) ||
                            error.contains("Invalid emergency shutdown", ignoreCase = true) ||
                            error.contains("Неверный пароль", ignoreCase = true)) {
                            emergencyShutdownError = error
                        } else if (error.contains("Failed to activate", ignoreCase = true)) {
                            emergencyShutdownError = error
                        }
                    }
                }

                // Закрываем диалог только при успехе
                LaunchedEffect(isEmergencyShutdownActive) {
                    if (isEmergencyShutdownActive) {
                        showEmergencyShutdownDialog = false
                        emergencyShutdownError = null
                        administrationViewModel.clearErrorMessage()
                    }
                }
            }
        }
    }
