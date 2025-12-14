package org.vengeful.citymanager.utilities.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_BACKUP
import org.vengeful.citymanager.ROUTE_BANK
import org.vengeful.citymanager.ROUTE_CLICKER
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_LIBRARY_ARTICLE
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_MEDIC_ORDERS
import org.vengeful.citymanager.ROUTE_MY_BANK
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.ROUTE_STOCKS
import org.vengeful.citymanager.screens.administration.AdministrationScreen
import org.vengeful.citymanager.screens.backup.BackupScreen
import org.vengeful.citymanager.screens.bank.BankScreen
import org.vengeful.citymanager.screens.clicker.ClickerScreen
import org.vengeful.citymanager.screens.commonLibrary.ArticleScreen
import org.vengeful.citymanager.screens.commonLibrary.CommonLibraryScreen
import org.vengeful.citymanager.screens.court.CourtScreen
import org.vengeful.citymanager.screens.main.MainScreen
import org.vengeful.citymanager.screens.medic.MedicOrdersScreen
import org.vengeful.citymanager.screens.medic.MedicScreen
import org.vengeful.citymanager.screens.my_bank.MyBankScreen
import org.vengeful.citymanager.screens.police.PoliceScreen
import org.vengeful.citymanager.screens.stocks.StockScreen


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
            CommonLibraryScreen(navController = navController)
        }
        composable(
            route = "$ROUTE_LIBRARY_ARTICLE/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.read {
                getInt("articleId")
            } ?: 0

            ArticleScreen(
                navController = navController,
                articleId = articleId
            )
        }
        composable(route = ROUTE_CLICKER) {
            ClickerScreen(navController = navController)
        }
        composable(route = ROUTE_MEDIC) {
            MedicScreen(navController = navController)
        }
        composable(route = ROUTE_MEDIC_ORDERS) {
            MedicOrdersScreen(navController = navController)
        }
        composable(route = ROUTE_BANK) {
            BankScreen(navController = navController)
        }
        composable(route = ROUTE_POLICE) {
            PoliceScreen(navController = navController)
        }
        composable(route = ROUTE_COURT) {
            CourtScreen(navController = navController)
        }
        composable(route = ROUTE_BACKUP) {
            BackupScreen(navController = navController)
        }
        composable(route = ROUTE_MY_BANK) {
            MyBankScreen(navController = navController)
        }
        composable(route = ROUTE_STOCKS) {
            StockScreen(navController = navController)
        }
    }
}
