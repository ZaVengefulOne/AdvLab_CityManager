package org.vengeful.citymanager.screens.clicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.severite
import org.jetbrains.compose.resources.painterResource
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun ClickerScreen(navController: NavController) {
    val viewModel: ClickerViewModel = koinViewModel()
    val clicks by viewModel.ebanatAmount.collectAsState()
    val theme = LocalTheme

    LaunchedEffect(Unit) {
        viewModel.loadClicks()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveClicks()
        }
    }

    VengBackground(
        modifier = Modifier,
        theme = theme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VengText(
                text = "Северит-коинов добыто: $clicks",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clickable { viewModel.incrementClicks() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painterResource(resource = Res.drawable.severite), null,
                        modifier = Modifier
                            .size(300.dp)
                    )
                }
                VengText(
                    "Кликни на меня, чтобы добыть!",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Назад"
            VengButton(
                onClick = {
                    viewModel.saveClicks()
                    navController.navigate(ROUTE_MAIN)
                },
                text = "Назад",
                theme = theme,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
