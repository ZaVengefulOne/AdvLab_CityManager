package org.vengeful.citymanager.screens.administration

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import org.vengeful.citymanager.BUILD_VERSION
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.administration.AdminChatWidget
import org.vengeful.citymanager.uikit.composables.administration.ControlLossIndicator
import org.vengeful.citymanager.uikit.composables.administration.EnterpriseCallWidget
import org.vengeful.citymanager.uikit.composables.administration.SeveriteRateGraph
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


    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var personToEdit by remember { mutableStateOf<Person?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }

    val getId = remember { mutableStateOf("") }
    val delId = remember { mutableStateOf("") }

    val bigSpacer = 120.dp
    val defaultSpacer = 24.dp
    val bigPadding = 16.dp
    val mediumPadding = 12.dp
    val defaultPadding = 8.dp

    val dividerColor = Color(0xFFD4AF37)
    val transColor = Color.Transparent

    var showShutdownAnimation by remember { mutableStateOf(false) }
    var showRestartAnimation by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

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
            theme = currentTheme
        )
        return
    }

    if (showRestartAnimation) {
        RestartAnimation(
            onComplete = { showRestartAnimation = false },
            theme = currentTheme
        )
        return
    }

    LaunchedEffect(Unit) {
        administrationViewModel.getPersons()
        administrationViewModel.getUsers()
        administrationViewModel.getAdminConfig()
        administrationViewModel.startConfigUpdates()
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
                }

            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = defaultSpacer),
                horizontalArrangement = Arrangement.spacedBy(defaultSpacer)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(mediumPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = stringResource(Res.string.user_list, users.size),
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        VengButton(
                            onClick = {
                                showRegisterDialog = true
                                administrationViewModel.getPersons()
                            },
                            text = stringResource(Res.string.add_new_person),
                            modifier = Modifier,
                            padding = 10.dp,
                            theme = currentTheme
                        )
                    }

                    if (users.isNotEmpty()) {
                        UserList(
                            users = users,
                            modifier = Modifier.fillMaxSize(),
                            onEditClick = { user ->
                                userToEdit = user
                            },
                            onDeleteClick = { user ->
                                userToDelete = user
                            },
                            onToggleActive = { user ->
                                administrationViewModel.toggleUserStatus(user.id, user.isActive)
                            },
                            theme = currentTheme
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f)
                                )
                                .border(
                                    1.dp,
                                    SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.2f)
                                )
                                .padding(defaultSpacer),
                            contentAlignment = Alignment.Center
                        ) {
                            VengText(
                                text = "Нет пользователей",
                                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Правая колонка - Жители
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        VengButton(
                            onClick = { showAddDialog = true },
                            text = stringResource(Res.string.add_new_person),
                            modifier = Modifier,
                            padding = 10.dp,
                            theme = currentTheme
                        )
                    }

                    if (persons.isNotEmpty()) {
                        PersonList(
                            persons = persons,
                            modifier = Modifier.fillMaxSize(),
                            onEditClick = { person ->
                                personToEdit = person
                            },
                            onDeleteClick = { person ->
                                personToDelete = person
                            },
                            theme = currentTheme
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f)
                                )
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
    }
}
