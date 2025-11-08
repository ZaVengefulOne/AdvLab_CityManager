package org.vengeful.citymanager.screens.main

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Library
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pen
import com.composables.icons.lucide.UtilityPole
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.di.KoinInjector
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.AuthDialog
import org.vengeful.citymanager.uikit.composables.icons.Home
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.terminal.TerminalControls
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.utilities.LocalTheme
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val viewModel = KoinInjector.mainViewModel

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showRestartAnimation by remember { mutableStateOf(false) }
    var showShutdownAnimation by remember { mutableStateOf(false) }
    var showLoggingDialog by remember { mutableStateOf(false) }

    val isLoggedData = viewModel.isLoggedData.collectAsState()

    val buttonSize = 100.dp

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
            theme = LocalTheme
        )
        return
    }
    VengBackground(
        theme = LocalTheme,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
//            ThemeSwitcher(
//                currentTheme = currentTheme,
//                onThemeChange = { newTheme ->
//                    LocalTheme = newTheme
//                    currentTheme = LocalTheme
//                }
//            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Тут будет диалог входа - ", color = Color.White, style = MaterialTheme.typography.titleMedium)
                VengButton(
                    onClick = {
                        showLoggingDialog = true
                    },
                    text = "Войти",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
            }
        }
        // TODO: Добавить сверху индикаторы доступности
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Тут будут индикаторы доступности",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                VengButton(
                    onClick = { navController.navigate(ROUTE_ADMINISTRATION) },
                    content = {
                        Icon(
                            Home,
                            "",
                            tint = Color.White,
                        )
                    },
                    text = "Администрация",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
                VengButton(
                    onClick = { navController.navigate(ROUTE_COURT) },
                    content = {
                        Icon(Lucide.Pen, "", tint = Color.White)
                    },
                    text = "Суд",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
                VengButton(
                    onClick = { navController.navigate(ROUTE_COMMON_LIBRARY) },
                    content = {
                        Icon(Lucide.Library, "", tint = Color.White)
                    },
                    text = "Городская библиотека",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
                VengButton(
                    onClick = { navController.navigate(ROUTE_MEDIC) },
                    content = {
                        Icon(Lucide.Heart, "", tint = Color.White)
                    },
                    text = "Больница",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
                VengButton(
                    onClick = { navController.navigate(ROUTE_POLICE) },
                    content = {
                        Icon(Lucide.UtilityPole, "", tint = Color.White)
                    },
                    text = "Полиция",
                    theme = LocalTheme,
                    modifier = Modifier.size(buttonSize)
                )
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
                onDismiss = { showLoggingDialog = false },
                onLogin = { login, password -> run {
                    viewModel.login(login, password)
                } },
                onLogged = { showLoggingDialog = false },
                theme = LocalTheme
            )
        }
    }
}
