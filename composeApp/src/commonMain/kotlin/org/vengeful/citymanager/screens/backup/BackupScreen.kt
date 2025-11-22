package org.vengeful.citymanager.screens.backup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.screens.main.MainViewModel
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun BackupScreen(navController: NavController) {
    val mainViewModel: MainViewModel = koinViewModel()
    val backupViewModel: BackupViewModel = koinViewModel()

    val rights = mainViewModel.rights.collectAsState().value
    val isLoading = backupViewModel.isLoading.collectAsState().value
    val errorMessage = backupViewModel.errorMessage.collectAsState().value

    // Проверка прав Joker
    if (!rights.contains(Rights.Joker)) {
        navController.navigate(ROUTE_MAIN)
        return
    }

    LaunchedEffect(Unit) {
        if (!rights.contains(Rights.Joker)) {
            navController.navigate(ROUTE_MAIN)
        }
    }

    VengBackground(
        theme = LocalTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Игровой бэкап",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VengButton(
                    onClick = {
                        backupViewModel.downloadGameBackup("html")
                    },
                    text = "Скачать HTML",
                    theme = LocalTheme,
                    enabled = !isLoading
                )

                VengButton(
                    onClick = {
                        backupViewModel.downloadGameBackup("markdown")
                    },
                    text = "Скачать Markdown",
                    theme = LocalTheme,
                    enabled = !isLoading
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            VengButton(
                onClick = { navController.navigate(ROUTE_MAIN) },
                text = "Назад",
                theme = LocalTheme
            )
        }
    }
}