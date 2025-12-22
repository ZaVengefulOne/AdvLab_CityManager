package org.vengeful.citymanager.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.administration_name
import citymanager.composeapp.generated.resources.app_name
import citymanager.composeapp.generated.resources.bank_title
import citymanager.composeapp.generated.resources.clicker_name
import citymanager.composeapp.generated.resources.coat
import citymanager.composeapp.generated.resources.common_library_name
import citymanager.composeapp.generated.resources.court_name
import citymanager.composeapp.generated.resources.login
import citymanager.composeapp.generated.resources.logout
import citymanager.composeapp.generated.resources.medic_name
import citymanager.composeapp.generated.resources.police_name
import citymanager.composeapp.generated.resources.welcome_message
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.FileQuestion
import com.composables.icons.lucide.FlaskConical
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Library
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MousePointerClick
import com.composables.icons.lucide.Newspaper
import com.composables.icons.lucide.Pen
import com.composables.icons.lucide.PiggyBank
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.UtilityPole
import com.composables.icons.lucide.Wallet
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.BUILD_VERSION
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_BACKUP
import org.vengeful.citymanager.ROUTE_BANK
import org.vengeful.citymanager.ROUTE_CLICKER
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_MY_BANK
import org.vengeful.citymanager.ROUTE_NEWS
import org.vengeful.citymanager.ROUTE_NIIS
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.ROUTE_STOCKS
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.data.users.states.LoginUiState
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.dialogs.AuthDialog
import org.vengeful.citymanager.uikit.composables.icons.Home
import org.vengeful.citymanager.uikit.composables.main.ScreenButtonCard
import org.vengeful.citymanager.uikit.composables.terminal.TerminalControls
import org.vengeful.citymanager.uikit.composables.misc.AccessIndicator
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import org.vengeful.citymanager.utilities.ScreenData
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
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка смены темы (слева)
            ThemeSwitcher(
                currentTheme = currentTheme,
                onThemeChange = { newTheme ->
                    LocalTheme = newTheme
                    currentTheme = LocalTheme
                },
                modifier = Modifier
                    .size(buttonSize)
                    .weight(0.2f),
            )

            // Название (центр)
            VengText(
                text = stringResource(Res.string.app_name, BUILD_VERSION),
                color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .padding(horizontal = defaultPadding, vertical = defaultPadding)
                    .weight(0.6f),
            )

            // Кнопка входа/выхода (справа)
            if (isLoggedData.value) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.2f),
                ) {
                    VengButton(
                        onClick = { viewModel.logout() },
                        text = stringResource(Res.string.logout),
                        theme = LocalTheme,
                        modifier = Modifier
                            .padding(bottom = defaultPadding)
                    )
                    VengText(
                        text = stringResource(Res.string.welcome_message, username.value),
                        color = colorTint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(horizontal = defaultPadding)
                    )
                }
            } else {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val screens = listOf(
                ScreenData(
                    route = ROUTE_ADMINISTRATION,
                    icon = { Icon(Home, null, tint = Color.White) },
                    text = stringResource(Res.string.administration_name)
                ),
                ScreenData(
                    route = ROUTE_COURT,
                    icon = { Icon(Lucide.Pen, null, tint = colorTint) },
                    text = stringResource(Res.string.court_name)
                ),
                ScreenData(
                    route = ROUTE_COMMON_LIBRARY,
                    icon = { Icon(Lucide.Library, null, tint = colorTint) },
                    text = stringResource(Res.string.common_library_name)
                ),
                ScreenData(
                    route = ROUTE_NEWS,
                    icon = { Icon(Lucide.Newspaper, null, tint = colorTint) },
                    text = "Новости"
                ),
                ScreenData(
                    route = ROUTE_CLICKER,
                    icon = { Icon(Lucide.MousePointerClick, null, tint = colorTint) },
                    text = stringResource(Res.string.clicker_name)
                ),
                null,
                ScreenData(
                    route = ROUTE_MEDIC,
                    icon = { Icon(Lucide.Heart, null, tint = colorTint) },
                    text = stringResource(Res.string.medic_name)
                ),
                ScreenData(
                    route = ROUTE_BANK,
                    icon = { Icon(Lucide.PiggyBank, null, tint = colorTint) },
                    text = stringResource(Res.string.bank_title)
                ),
                ScreenData(
                    route = ROUTE_POLICE,
                    icon = { Icon(Lucide.UtilityPole, null, tint = colorTint) },
                    text = stringResource(Res.string.police_name)
                ),
                ScreenData(
                    route = ROUTE_STOCKS,
                    icon = { Icon(Lucide.TrendingUp, null, tint = colorTint) },
                    text = "Акции"
                ),
                ScreenData(
                    route = ROUTE_NIIS,
                    icon = { Icon(Lucide.FlaskConical, null, tint = colorTint) },
                    text = "НИИС"
                ),
            )
            val finalScreens = if (viewModel.hasAccessToScreen(ROUTE_MY_BANK)) {
                screens + ScreenData(
                    route = ROUTE_MY_BANK,
                    icon = { Icon(Lucide.Wallet, null, tint = colorTint) },
                    text = "Мой Банк"
                )
            } else {
                screens + ScreenData(
                    route = ROUTE_MAIN,
                    icon = { Icon(Lucide.FileQuestion, null, tint = colorTint) },
                    text = "Заблокировано"
                )
            }

            val gridItemSize = 200.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(defaultPadding)
                ) {
                    itemsIndexed(finalScreens) { index, screenData ->
                        Box(
                            modifier = Modifier
                                .size(gridItemSize)
                                .padding(defaultPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                index == 4 -> {
                                    Image(
                                        painter = painterResource(Res.drawable.coat),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(defaultPadding)
                                    )
                                }

                                screenData != null -> {
                                    ScreenButtonCard(
                                        onClick = { navController.navigate(screenData.route) },
                                        icon = screenData.icon,
                                        text = screenData.text,
                                        hasAccess = viewModel.hasAccessToScreen(screenData.route),
                                        theme = LocalTheme,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
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
            LaunchedEffect(loginState.value) {
                if (loginState.value is LoginUiState.Success) {
                    showLoggingDialog = false
                    viewModel.resetLoginState()
                }
            }
        }
    }
}
