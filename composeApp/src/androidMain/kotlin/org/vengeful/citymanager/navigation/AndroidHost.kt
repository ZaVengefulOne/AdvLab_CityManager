package org.vengeful.citymanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.vengeful.citymanager.ui.AndroidMainScreen
import org.vengeful.citymanager.ui.UserManagementScreen


@Composable
fun AndroidHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ANDROID_ROUTE_MAIN,
    ) {
        composable(route = ANDROID_ROUTE_MAIN) {
            AndroidMainScreen(navController = navController)
        }
        composable(route = ANDROID_ROUTE_CREATE_USER) {
            UserManagementScreen(navController = navController)
        }
    }
}


const val ANDROID_ROUTE_MAIN = "android_main"
const val ANDROID_ROUTE_CREATE_USER = "android_create_user"
