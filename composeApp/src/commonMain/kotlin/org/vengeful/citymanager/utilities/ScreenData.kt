package org.vengeful.citymanager.utilities

import androidx.compose.runtime.Composable

data class ScreenData(
    val route: String,
    val icon: @Composable () -> Unit,
    val text: String
)
