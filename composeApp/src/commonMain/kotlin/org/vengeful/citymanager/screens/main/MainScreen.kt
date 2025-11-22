package org.vengeful.citymanager.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.administration_name
import citymanager.composeapp.generated.resources.bank_title
import citymanager.composeapp.generated.resources.clicker_name
import citymanager.composeapp.generated.resources.common_library_name
import citymanager.composeapp.generated.resources.court_name
import citymanager.composeapp.generated.resources.login
import citymanager.composeapp.generated.resources.logout
import citymanager.composeapp.generated.resources.medic_name
import citymanager.composeapp.generated.resources.police_name
import citymanager.composeapp.generated.resources.welcome_message
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Library
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MousePointerClick
import com.composables.icons.lucide.Pen
import com.composables.icons.lucide.PiggyBank
import com.composables.icons.lucide.UtilityPole
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_BACKUP
import org.vengeful.citymanager.ROUTE_BANK
import org.vengeful.citymanager.ROUTE_CLICKER
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.data.users.states.LoginUiState
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.dialogs.AuthDialog
import org.vengeful.citymanager.uikit.composables.icons.Home
import org.vengeful.citymanager.uikit.composables.terminal.TerminalControls
import org.vengeful.citymanager.uikit.composables.misc.AccessIndicator
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.utilities.LocalTheme
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val viewModel: MainViewModel = koinViewModel()

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showRestartAnimation by remember { mutableStateOf(false) }
    var showShutdownAnimation by remember { mutableStateOf(false) }
    var showLoggingDialog by remember { mutableStateOf(false) }

    val isLoggedData = viewModel.isLoggedData.collectAsState()
    val loginState = viewModel.loginState.collectAsState()
    val username = viewModel.username.collectAsState()

    val buttonSize = 100.dp
    val bigPadding = 16.dp
    val defaultPadding = 8.dp

    val colorTint = Color.White

    if (showShutdownAnimation) {
        ShutdownAnimation(
            onComplete = { exitProcess(0) },
            theme = LocalTheme
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
    VengBackground(
        theme = currentTheme,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            ThemeSwitcher(
                currentTheme = currentTheme,
                onThemeChange = { newTheme ->
                    LocalTheme = newTheme
                    currentTheme = LocalTheme
                },
                modifier = Modifier.size(buttonSize),
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = bigPadding, vertical = defaultPadding)
            ) {
                if (isLoggedData.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(bigPadding)
                    ) {
                        Text(
                            text = stringResource(Res.string.welcome_message, username.value),
                            color = colorTint,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        VengButton(
                            onClick = { viewModel.logout() },
                            text = stringResource(Res.string.logout),
                            theme = LocalTheme,
                            modifier = Modifier.size(buttonSize)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VengButton(
                            onClick = {
                                showLoggingDialog = true
                            },
                            text = stringResource(Res.string.login),
                            theme = LocalTheme,
                            modifier = Modifier.size(buttonSize)
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Администрация
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_ADMINISTRATION),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_ADMINISTRATION) },
                        content = {
                            Icon(
                                Home,
                                null,
                                tint = Color.White,
                            )
                        },
                        text = stringResource(Res.string.administration_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_ADMINISTRATION)
                    )
                }

                // Суд
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_COURT),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_COURT) },
                        content = {
                            Icon(Lucide.Pen, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.court_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_COURT)
                    )
                }

                // Городская библиотека
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_COMMON_LIBRARY),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_COMMON_LIBRARY) },
                        content = {
                            Icon(Lucide.Library, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.common_library_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_COMMON_LIBRARY)
                    )
                }

                // Кликер
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_CLICKER),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_CLICKER) },
                        content = {
                            Icon(Lucide.MousePointerClick, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.clicker_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_CLICKER)
                    )
                }

                // Больница
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_MEDIC),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_MEDIC) },
                        content = {
                            Icon(Lucide.Heart, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.medic_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_MEDIC)
                    )
                }

                // Банк
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_BANK),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_BANK) },
                        content = {
                            Icon(Lucide.PiggyBank, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.bank_title),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_BANK)
                    )
                }

                // Полиция
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = defaultPadding)
                ) {
                    AccessIndicator(
                        hasAccess = viewModel.hasAccessToScreen(ROUTE_POLICE),
                        theme = LocalTheme,
                        modifier = Modifier.padding(bottom = defaultPadding)
                    )
                    VengButton(
                        onClick = { navController.navigate(ROUTE_POLICE) },
                        content = {
                            Icon(Lucide.UtilityPole, null, tint = colorTint)
                        },
                        text = stringResource(Res.string.police_name),
                        theme = LocalTheme,
                        modifier = Modifier.size(buttonSize),
                        enabled = viewModel.hasAccessToScreen(ROUTE_POLICE)
                    )
                }

                if (viewModel.hasAccessToScreen(ROUTE_BACKUP)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = defaultPadding)
                    ) {
                        AccessIndicator(
                            hasAccess = viewModel.hasAccessToScreen(ROUTE_BACKUP),
                            theme = LocalTheme,
                            modifier = Modifier.padding(bottom = defaultPadding)
                        )
                        VengButton(
                            onClick = { navController.navigate(ROUTE_BACKUP) },
                            content = {
                                Icon(Lucide.Download, null, tint = colorTint)
                            },
                            text = "Бэкап",
                            theme = LocalTheme,
                            modifier = Modifier.size(buttonSize),
                            enabled = viewModel.hasAccessToScreen(ROUTE_BACKUP)
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            TerminalControls(
                onShutdown = { showShutdownAnimation = true },
                onRestart = { showRestartAnimation = true },
                theme = LocalTheme,
                modifier = Modifier.size(buttonSize)
            )
        }
        if (showLoggingDialog) {
            AuthDialog(
                onDismiss = { 
                    showLoggingDialog = false
                    viewModel.resetLoginState()
                },
                onLogin = { login, password ->
                    viewModel.login(login, password)
                },
                isLoading = loginState.value is LoginUiState.Loading,
                errorMessage = when (val state = loginState.value) {
                    is LoginUiState.Error -> state.message
                    else -> null
                },
                theme = LocalTheme
            )
            
            // Закрыть диалог при успешном логине
            LaunchedEffect(loginState.value) {
                if (loginState.value is LoginUiState.Success) {
                    showLoggingDialog = false
                    viewModel.resetLoginState()
                }
            }
        }
    }
}
