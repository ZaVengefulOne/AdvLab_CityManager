package org.vengeful.citymanager.screens.administration.userManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.screens.administration.AdministrationViewModel
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.dialogs.RegisterDialog
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.person.PersonDialog
import org.vengeful.citymanager.uikit.composables.person.PersonDossierDialog
import org.vengeful.citymanager.uikit.composables.person.PersonEditDialog
import org.vengeful.citymanager.uikit.composables.person.PersonList
import org.vengeful.citymanager.uikit.composables.user.UserEditDialog
import org.vengeful.citymanager.uikit.composables.user.UserList
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengTabRow
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme
import androidx.compose.runtime.collectAsState
import citymanager.composeapp.generated.resources.back
import citymanager.composeapp.generated.resources.base_empty
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.screens.police.CaseViewModel
import org.koin.core.context.GlobalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersAndPersonsScreen(navController: NavController) {
    val administrationViewModel: AdministrationViewModel = koinViewModel()
    val persons = administrationViewModel.persons.collectAsState().value
    val users = administrationViewModel.users.collectAsState().value

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var selectedTab by remember { mutableStateOf(0) } // 0 - пользователи, 1 - жители
    var userSearchQuery by remember { mutableStateOf("") }
    var personSearchQuery by remember { mutableStateOf("") }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var personToEdit by remember { mutableStateOf<Person?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var personForDossier by remember { mutableStateOf<Person?>(null) }
    var bankBalance by remember { mutableStateOf<Double?>(null) }
    var hasBankAccount by remember { mutableStateOf(false) }

    val caseViewModel: CaseViewModel = koinViewModel()
    val bankInteractor: IBankInteractor = remember { GlobalContext.get().get() }
    val scope = rememberCoroutineScope()
    val casesFromViewModel by caseViewModel.cases.collectAsState()

    val defaultSpacer = 24.dp
    val mediumPadding = 12.dp
    val defaultPadding = 8.dp

    LaunchedEffect(Unit) {
        administrationViewModel.getPersons()
        administrationViewModel.getUsers()
    }

    // Фильтрация пользователей
    val filteredUsers = remember(users, userSearchQuery) {
        if (userSearchQuery.isBlank()) {
            users
        } else {
            val searchText = userSearchQuery.lowercase()
            users.filter { user ->
                "${user.username} ${user.id}".lowercase().contains(searchText)
            }
        }
    }

    // Фильтрация жителей
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
            // Заголовок и навигация
            Row(
                horizontalArrangement = Arrangement.spacedBy(defaultSpacer),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeSwitcher(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        LocalTheme = newTheme
                        currentTheme = LocalTheme
                    },
                    modifier = Modifier.weight(0.1f),
                )

                VengText(
                    text = if (selectedTab == 0) "Пользователи" else "Жители",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )

                VengButton(
                    text = stringResource(Res.string.back),
                    onClick = {
                        navController.popBackStack()
                    },
                    theme = currentTheme,
                    modifier = Modifier.weight(0.15f)
                )
            }

            // Переключатель вкладок и кнопка добавления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengTabRow(
                    selectedTabIndex = selectedTab,
                    tabs = listOf("Пользователи (${users.size})", "Жители (${persons.size})"),
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.weight(1f),
                    theme = currentTheme
                )
                
                VengButton(
                    text = if (selectedTab == 0) "Добавить пользователя" else "Добавить жителя",
                    onClick = {
                        if (selectedTab == 0) {
                            showAddUserDialog = true
                        } else {
                            showAddPersonDialog = true
                        }
                    },
                    theme = currentTheme,
                    modifier = Modifier.padding(start = 8.dp),
                    padding = 12.dp
                )
            }

            // Поле поиска
            VengTextField(
                value = if (selectedTab == 0) userSearchQuery else personSearchQuery,
                onValueChange = {
                    if (selectedTab == 0) {
                        userSearchQuery = it
                    } else {
                        personSearchQuery = it
                    }
                },
                label = "Поиск",
                placeholder = if (selectedTab == 0) "Введите username или ID..." else "Введите имя, фамилию или ID...",
                modifier = Modifier.fillMaxWidth(),
                theme = currentTheme
            )

            // Контент вкладки
            when (selectedTab) {
                0 -> {
                    // Вкладка пользователей
                    if (filteredUsers.isNotEmpty()) {
                        UserList(
                            users = filteredUsers,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f, fill = false),
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
                                .weight(1f, fill = false)
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
                                text = if (userSearchQuery.isBlank()) "Нет пользователей" else "Ничего не найдено",
                                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                1 -> {
                    // Вкладка жителей
                    if (filteredPersons.isNotEmpty()) {
                        PersonList(
                            persons = filteredPersons,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f, fill = false),
                            onEditClick = { person ->
                                personToEdit = person
                            },
                            onDeleteClick = { person ->
                                personToDelete = person
                            },
                            onPersonClick = { person ->
                                personForDossier = person
                            },
                            theme = currentTheme
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f, fill = false)
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
                                text = if (personSearchQuery.isBlank()) stringResource(Res.string.base_empty) else "Ничего не найдено",
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

        // Диалог добавления пользователя
        if (showAddUserDialog) {
            val registerState = administrationViewModel.registerState.collectAsState().value
            RegisterDialog(
                onDismiss = { 
                    showAddUserDialog = false
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
            
            // Закрываем диалог при успешной регистрации
            LaunchedEffect(registerState) {
                if (registerState is RegisterUiState.Success) {
                    showAddUserDialog = false
                    administrationViewModel.resetRegisterState()
                }
            }
        }

        // Диалог добавления жителя
        if (showAddPersonDialog) {
            PersonDialog(
                onDismiss = { showAddPersonDialog = false },
                onAddPerson = { person ->
                    administrationViewModel.addPerson(person)
                    administrationViewModel.getPersons()
                    showAddPersonDialog = false
                },
                theme = currentTheme
            )
        }

        // Диалог досье жителя
        personForDossier?.let { person ->
            LaunchedEffect(person.id) {
                scope.launch {
                    // Загружаем дела, где житель является подозреваемым
                    try {
                        caseViewModel.loadCasesBySuspect(person.id)
                    } catch (e: Exception) {
                        // Ошибка будет обработана в ViewModel
                    }

                    // Загружаем информацию о банковском счёте
                    try {
                        val account = bankInteractor.getBankAccountByPersonId(person.id)
                        hasBankAccount = account != null
                        // Баланс хранится в Person, а не в BankAccount
                        bankBalance = person.balance
                    } catch (e: Exception) {
                        hasBankAccount = false
                        bankBalance = null
                    }
                }
            }

            // Обновляем список дел из ViewModel
            val currentCasesAsSuspect = casesFromViewModel.filter { it.suspectPersonId == person.id }

            PersonDossierDialog(
                person = person,
                casesAsSuspect = currentCasesAsSuspect,
                bankBalance = bankBalance,
                hasBankAccount = hasBankAccount,
                onDismiss = {
                    personForDossier = null
                    bankBalance = null
                    hasBankAccount = false
                },
                theme = currentTheme
            )
        }
    }
}

