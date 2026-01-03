package org.vengeful.citymanager.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.navigation.*
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun AndroidMainScreen(navController: NavController) {
    VengBackground(
        theme = LocalTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            VengText(
                text = "Главное меню",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                fontSize = 32.sp,
            )

            VengButton(
                onClick = { navController.navigate(ANDROID_ROUTE_CREATE_USER) },
                text = "Создать пользователя",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )

            VengButton(
                onClick = { navController.navigate(ANDROID_ROUTE_BACKUP) },
                text = "Создать бэкап",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )

            VengButton(
                onClick = { navController.navigate(ANDROID_ROUTE_USERS_LIST) },
                text = "Управление пользователями",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )

            VengButton(
                onClick = { navController.navigate(ANDROID_ROUTE_PERSONS_LIST) },
                text = "Управление жителями",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
