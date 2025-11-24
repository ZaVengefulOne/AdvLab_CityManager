package org.vengeful.citymanager.screens.medic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.back
import citymanager.composeapp.generated.resources.common_library_name
import citymanager.composeapp.generated.resources.medic_name
import citymanager.composeapp.generated.resources.police_name
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun MedicScreen(navController: NavController) {
    var currentTheme by remember { mutableStateOf(LocalTheme) }
    VengBackground(
        theme = currentTheme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Здесь будет ${stringResource(resource = Res.string.medic_name)}",
                color = Color.White,)
            VengButton(
                onClick = { navController.popBackStack() },
                text = stringResource(Res.string.back),
                theme = currentTheme
            )
        }
    }
}
