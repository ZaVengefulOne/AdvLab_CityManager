package org.vengeful.citymanager.utilities.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_CLICKER
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.screens.administration.AdministrationScreen
import org.vengeful.citymanager.screens.clicker.ClickerScreen
import org.vengeful.citymanager.screens.commonLibrary.CommonLibraryScreen
import org.vengeful.citymanager.screens.court.CourtScreen
import org.vengeful.citymanager.screens.main.MainScreen
import org.vengeful.citymanager.screens.medic.MedicScreen
import org.vengeful.citymanager.screens.police.PoliceScreen


@Composable
fun Host() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN,
    ) {
        composable(route = ROUTE_MAIN) {
            MainScreen(navController = navController)
        }
        composable(route = ROUTE_ADMINISTRATION) {
            AdministrationScreen(navController = navController)
        }
        composable(route = ROUTE_COMMON_LIBRARY) {
            CommonLibraryScreen()
        }
        composable(route = ROUTE_CLICKER) {
            ClickerScreen(navController = navController)
        }
        composable(route = ROUTE_MEDIC) {
            MedicScreen()
        }
        composable(route = ROUTE_POLICE) {
            PoliceScreen()
        }
        composable(route = ROUTE_COURT) {
            CourtScreen()
        }
    }
}