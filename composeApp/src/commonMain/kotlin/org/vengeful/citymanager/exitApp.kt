package org.vengeful.citymanager

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.event.WindowEvent

// Функция для закрытия приложения на Desktop
fun exitApplication() {
    // Для Compose Desktop
    val window = Toolkit.getDefaultToolkit().getDesktopProperty("awt.window") as? java.awt.Window
    window?.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))

    // Альтернативный способ - системный выход
    // System.exit(0)
}

