package org.vengeful.citymanager.audio

import androidx.compose.runtime.Composable

expect class SoundPlayer {
    fun playShutdownSound()
    fun playStartupSound()
    fun playClickSound() // Звук кликанья при настройке элементов
    fun playSystemWorkingSound() // Звук работы системы при очистке
    fun stopSystemWorkingSound() // Остановить звук работы системы
    fun playSystemShutdownSound() // Звук выключения системы при истечении таймера
    fun release()
}

@Composable
expect fun rememberSoundPlayer(): SoundPlayer

