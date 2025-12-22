package org.vengeful.citymanager.screens.niis

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.ROUTE_NIIS_CLEANING
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun NIISMainScreen(navController: NavController) {
    val currentTheme = LocalTheme

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VengText(
                    text = "НИИС",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                VengText(
                    text = "Научно Исследовательский \n Институт Северита",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp),
                    lineHeight = 24.sp,
                    maxLines = 2
                )
            }

            Column {
                VengButton(
                    onClick = { navController.navigate(ROUTE_NIIS_CLEANING) },
                    text = "Начать процесс очистки северита",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = "Назад",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            }
        }
    }
}
