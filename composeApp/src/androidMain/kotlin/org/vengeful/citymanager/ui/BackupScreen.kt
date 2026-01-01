package org.vengeful.citymanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.data.backup.AndroidBackupInteractor
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.screens.main.MainViewModel
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import org.koin.core.context.GlobalContext

@Composable
fun BackupScreen(navController: NavController) {
    val mainViewModel: MainViewModel = koinViewModel()
    val context = LocalContext.current
    val authManager: AuthManager = remember { GlobalContext.get().get() }
    val backupInteractor = remember { AndroidBackupInteractor(authManager, context) }

    val rights = mainViewModel.rights.collectAsState().value
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var downloadTrigger by remember { mutableStateOf(0) }

    // Проверка прав Joker
    LaunchedEffect(Unit) {
        if (!rights.contains(Rights.Joker)) {
            navController.popBackStack()
        }
    }

    if (!rights.contains(Rights.Joker)) {
        return
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
            VengText(
                text = "Игровой бэкап",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            if (successMessage != null) {
                VengText(
                    text = successMessage!!,
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                )
            }

            if (errorMessage != null) {
                VengText(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
            }

            VengButton(
                onClick = {
                    downloadTrigger++
                },
                text = "Скачать HTML бэкап",
                theme = LocalTheme,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            LaunchedEffect(downloadTrigger) {
                if (downloadTrigger > 0) {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    try {
                        backupInteractor.downloadGameBackup("html")
                        successMessage = "Бэкап успешно сохранён в папку Downloads"
                        kotlinx.coroutines.delay(3000)
                        successMessage = null
                    } catch (e: Exception) {
                        errorMessage = "Ошибка: ${e.message}"
                        kotlinx.coroutines.delay(5000)
                        errorMessage = null
                    } finally {
                        isLoading = false
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            }

            VengButton(
                onClick = { navController.navigateUp() },
                text = "Назад",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

