package org.vengeful.citymanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vengeful.citymanager.di.initKoin
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.utilities.navigation.Host

@Composable
@Preview
fun App() {
    initKoin()
    MaterialTheme {
        Host()
    }
}