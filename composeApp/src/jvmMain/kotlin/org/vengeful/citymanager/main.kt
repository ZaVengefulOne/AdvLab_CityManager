package org.vengeful.citymanager

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val windowState = WindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Система Государственного Контроля",
        state = windowState,
        icon = painterResource("icon.ico")
    ) {
        App()
    }
}
