package org.vengeful.cityManager

import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        AdminApp()
    }
}