package org.vengeful.citymanager.screens.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.CallIndicator
import org.vengeful.citymanager.uikit.composables.EmergencyButton
import org.vengeful.citymanager.uikit.composables.bank.BankAccountDialog
import org.vengeful.citymanager.uikit.composables.bank.BankAccountEditDialog
import org.vengeful.citymanager.uikit.composables.bank.BankAccountGrid
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.toRights
import org.vengeful.citymanager.uikit.composables.personnel.PasswordDialog
import org.vengeful.citymanager.uikit.composables.personnel.PersonnelManagementDialog
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState

@Composable
fun BankScreen(navController: NavController) {
    val viewModel: BankViewModel = koinViewModel()
    val persons = viewModel.persons.collectAsState().value
    val bankAccounts = viewModel.bankAccounts.collectAsState().value
    val callStatus = viewModel.callStatus.collectAsState().value

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }
    var personAccountsSearchQuery by remember { mutableStateOf("") }
    var enterpriseAccountsSearchQuery by remember { mutableStateOf("") }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPersonnelManagementDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val defaultSpacer = 16.dp
    val mediumPadding = 12.dp
    val defaultPadding = 8.dp

    LaunchedEffect(Unit) {
        viewModel.getPersons()
        viewModel.getBankAccounts()
        viewModel.startStatusCheck()
        // Сбрасываем состояние "нажата" при входе на экран
        viewModel.resetEmergencyButtonState()
    }

    VengBackground(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        theme = currentTheme,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(defaultSpacer),
            verticalArrangement = Arrangement.spacedBy(defaultSpacer),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(defaultSpacer),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeSwitcher(
                            currentTheme = currentTheme,
                            onThemeChange = { newTheme ->
                                LocalTheme = newTheme
                                currentTheme = LocalTheme
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        VengButton(
                            onClick = { showPasswordDialog = true },
                            text = "",
                            theme = currentTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            content = {
                                VengText(
                                    text = "Сотрудники",
                                    color = SeveritepunkThemes.getColorScheme(currentTheme).text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        )
                    }

                    // Тревожная кнопка справа от блока с кнопками
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                VengText(
                    text = stringResource(Res.string.bank_name),
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = defaultPadding),
                    textAlign = TextAlign.Center
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = stringResource(Res.string.back),
                    theme = currentTheme,
                    modifier = Modifier.weight(0.15f),
                )
            }

            CallIndicator(
                isCalled = callStatus?.isCalled ?: false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onDismiss = {
                    viewModel.resetCall()
                }
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Всего счетов: ${bankAccounts.size}",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                VengButton(
                    onClick = { showAddDialog = true },
                    text = stringResource(Res.string.add_bank_account),
                    modifier = Modifier,
                    padding = 10.dp,
                    theme = currentTheme
                )
            }

            // Разделение счетов на личные и корпоративные
            val personAccounts = remember(bankAccounts) {
                bankAccounts.filter { it.personId != null }
            }
            val enterpriseAccounts = remember(bankAccounts) {
                bankAccounts.filter { it.enterpriseName != null }
            }

            // Фильтрация личных счетов
            val filteredPersonAccounts = remember(personAccounts, personAccountsSearchQuery, persons) {
                if (personAccountsSearchQuery.isBlank()) {
                    personAccounts
                } else {
                    val searchText = personAccountsSearchQuery.lowercase()
                    personAccounts.filter { account ->
                        val person = persons.find { it.id == account.personId }
                        person?.let {
                            "${it.firstName} ${it.lastName} ${it.id} ${account.id}".lowercase().contains(searchText)
                        } ?: false
                    }
                }
            }

            // Фильтрация счетов предприятий
            val filteredEnterpriseAccounts = remember(enterpriseAccounts, enterpriseAccountsSearchQuery) {
                if (enterpriseAccountsSearchQuery.isBlank()) {
                    enterpriseAccounts
                } else {
                    val searchText = enterpriseAccountsSearchQuery.lowercase()
                    enterpriseAccounts.filter { account ->
                        "${account.enterpriseName} ${account.id}".lowercase().contains(searchText)
                    }
                }
            }

            // Личные счета
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(mediumPadding)
            ) {
                VengText(
                    text = "Личные счета (${personAccounts.size})",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                VengTextField(
                    value = personAccountsSearchQuery,
                    onValueChange = { personAccountsSearchQuery = it },
                    label = "Поиск личных счетов",
                    placeholder = "Введите имя, фамилию или ID...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = currentTheme
                )

                if (filteredPersonAccounts.isNotEmpty()) {
                    BankAccountGrid(
                        accounts = filteredPersonAccounts,
                        persons = persons,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        onAccountClick = { account ->
                            accountToEdit = account
                        },
                        theme = currentTheme
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.2f),
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(defaultSpacer),
                        contentAlignment = Alignment.Center
                    ) {
                        VengText(
                            text = if (personAccountsSearchQuery.isBlank()) "Нет личных счетов" else "Ничего не найдено",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(mediumPadding))

            // Счета предприятий
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(mediumPadding)
            ) {
                VengText(
                    text = "Счета предприятий (${enterpriseAccounts.size})",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                VengTextField(
                    value = enterpriseAccountsSearchQuery,
                    onValueChange = { enterpriseAccountsSearchQuery = it },
                    label = "Поиск счетов предприятий",
                    placeholder = "Введите название предприятия или ID...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = currentTheme
                )

                if (filteredEnterpriseAccounts.isNotEmpty()) {
                    BankAccountGrid(
                        accounts = filteredEnterpriseAccounts,
                        persons = persons,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        onAccountClick = { account ->
                            accountToEdit = account
                        },
                        theme = currentTheme
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.2f),
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(defaultSpacer),
                        contentAlignment = Alignment.Center
                    ) {
                        VengText(
                            text = if (enterpriseAccountsSearchQuery.isBlank()) "Нет счетов предприятий" else "Ничего не найдено",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Диалог создания банковского счета
        if (showAddDialog) {
            BankAccountDialog(
                persons = persons,
                onDismiss = { showAddDialog = false },
                onCreateAccount = { personId, enterpriseName, creditAmount, personBalance ->
                    viewModel.createBankAccount(personId, enterpriseName, creditAmount, personBalance)
                },
                theme = currentTheme
            )
        }

        // Диалог редактирования банковского счета
        accountToEdit?.let { account ->
            val personForAccount = persons.find { it.id == account.personId }
            BankAccountEditDialog(
                account = account,
                person = personForAccount,
                onDismiss = { accountToEdit = null },
                onSave = { updatedAccount, personBalance ->
                    viewModel.updateBankAccount(updatedAccount, personBalance)
                },
                onDelete = { accountId ->
                    viewModel.deleteBankAccount(accountId)
                },
                onCloseCredit = { accountId ->
                    viewModel.closeCredit(accountId)
                },
                theme = currentTheme
            )
        }

        // Диалог подтверждения удаления
        accountToDelete?.let { account ->
            DeleteConfirmationDialog(
                onDismiss = { accountToDelete = null },
                onConfirm = {
                    viewModel.deleteBankAccount(account.id)
                    accountToDelete = null
                },
                theme = currentTheme
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
            val personnel = viewModel.getPersonnelByRight(Enterprise.BANK.toRights())
            PersonnelManagementDialog(
                enterpriseRight = Enterprise.BANK.toRights(),
                allPersons = persons,
                personnel = personnel,
                isLoading = false,
                errorMessage = viewModel.errorMessage.collectAsState().value,
                onAddPerson = { person ->
                    viewModel.addRightToPerson(person.id, Enterprise.BANK.toRights())
                },
                onRemovePerson = { person ->
                    viewModel.removeRightFromPerson(person.id, Enterprise.BANK.toRights())
                },
                onDismiss = { showPersonnelManagementDialog = false },
                theme = currentTheme
            )
        }
        }
    }
}
