package org.vengeful.citymanager

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import citymanager.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val windowState = WindowState(
        placement = WindowPlacement.Fullscreen,
        position = WindowPosition.Aligned(Alignment.Center),
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Система Государственного Контроля",
        state = windowState,
        undecorated = true,
        icon = painterResource("icon.ico")
    ) {
        App()
    }
}
