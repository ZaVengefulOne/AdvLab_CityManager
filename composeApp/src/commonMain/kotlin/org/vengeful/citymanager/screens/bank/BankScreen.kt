package org.vengeful.citymanager.screens.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import org.vengeful.citymanager.uikit.composables.bank.BankAccountDialog
import org.vengeful.citymanager.uikit.composables.bank.BankAccountEditDialog
import org.vengeful.citymanager.uikit.composables.bank.BankAccountGrid
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun BankScreen(navController: NavController) {
    val viewModel: BankViewModel = koinViewModel()
    val persons = viewModel.persons.collectAsState().value
    val bankAccounts = viewModel.bankAccounts.collectAsState().value

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }

    val defaultSpacer = 16.dp
    val mediumPadding = 12.dp
    val defaultPadding = 8.dp

    LaunchedEffect(Unit) {
        viewModel.getPersons()
        viewModel.getBankAccounts()
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
            // Заголовок с кнопками
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

                Text(
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

            // Панель управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
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

            // Список счетов в виде Grid (компактный, не занимает всё пространство)
            if (bankAccounts.isNotEmpty()) {
                BankAccountGrid(
                    accounts = bankAccounts,
                    persons = persons,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 600.dp), // Ограничиваем высоту
                    onAccountClick = { account ->
                        accountToEdit = account
                    },
                    theme = currentTheme
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                    Text(
                        text = stringResource(Res.string.bank_empty),
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Диалог создания банковского счета
        if (showAddDialog) {
            BankAccountDialog(
                persons = persons,
                onDismiss = { showAddDialog = false },
                onCreateAccount = { personId, enterpriseName, depositAmount, creditAmount -> // НОВОЕ
                    viewModel.createBankAccount(personId, enterpriseName, depositAmount, creditAmount) // НОВОЕ
                },
                theme = currentTheme
            )
        }

        // Диалог редактирования банковского счета
        accountToEdit?.let { account ->
            BankAccountEditDialog(
                account = account,
                onDismiss = { accountToEdit = null },
                onSave = { updatedAccount ->
                    viewModel.updateBankAccount(updatedAccount)
                },
                onDelete = { accountId -> // НОВОЕ: callback для удаления
                    viewModel.deleteBankAccount(accountId)
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
    }
}