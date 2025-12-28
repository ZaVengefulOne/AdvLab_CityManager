package org.vengeful.citymanager.audio

import androidx.compose.runtime.Composable

expect class SoundPlayer {
    fun playShutdownSound()
    fun playStartupSound()
    fun release()
}

@Composable
expect fun rememberSoundPlayer(): SoundPlayer

