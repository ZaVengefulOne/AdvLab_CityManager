package org.vengeful.citymanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.core.context.GlobalContext
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.ui.AndroidMainScreen
import org.vengeful.citymanager.ui.UserManagementScreen
import org.vengeful.citymanager.ui.BackupScreen
import org.vengeful.citymanager.ui.UsersListScreen
import org.vengeful.citymanager.ui.PersonsListScreen
import org.vengeful.citymanager.ui.auth.HackerLoginScreen


@Composable
fun AndroidHost() {
    val navController = rememberNavController()
    val userInteractor: IUserInteractor = remember { GlobalContext.get().get() }
    val authManager: AuthManager = remember { GlobalContext.get().get() }

    // Автоматический логин с правами joker при старте
    LaunchedEffect(Unit) {
        if (!authManager.isLoggedIn()) {
            try {
                userInteractor.loginAsJoker()
            } catch (e: Exception) {
                // Логируем ошибку, но не блокируем приложение
                println("Failed to auto-login as joker: ${e.message}")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ANDROID_ROUTE_LOGIN,
    ) {
        composable(route = ANDROID_ROUTE_LOGIN) {
            HackerLoginScreen(
                onHackSuccess = {
                    // Переходим на главный экран и очищаем стек навигации
                    navController.navigate(ANDROID_ROUTE_MAIN) {
                        popUpTo(ANDROID_ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(route = ANDROID_ROUTE_MAIN) {
            AndroidMainScreen(navController = navController)
        }
        composable(route = ANDROID_ROUTE_CREATE_USER) {
            UserManagementScreen(navController = navController)
        }
        composable(route = ANDROID_ROUTE_BACKUP) {
            BackupScreen(navController = navController)
        }
        composable(route = ANDROID_ROUTE_USERS_LIST) {
            UsersListScreen(navController = navController)
        }
        composable(route = ANDROID_ROUTE_PERSONS_LIST) {
            PersonsListScreen(navController = navController)
        }
    }
}


const val ANDROID_ROUTE_LOGIN = "android_login"
const val ANDROID_ROUTE_MAIN = "android_main"
const val ANDROID_ROUTE_CREATE_USER = "android_create_user"
const val ANDROID_ROUTE_BACKUP = "android_backup"
const val ANDROID_ROUTE_USERS_LIST = "android_users_list"
const val ANDROID_ROUTE_PERSONS_LIST = "android_persons_list"
