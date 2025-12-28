package org.vengeful.citymanager.uikit.animations

import androidx.compose.runtime.Composable
import org.vengeful.citymanager.audio.SoundPlayer
import org.vengeful.citymanager.uikit.ColorTheme

@Composable
fun RestartAnimation(
    onComplete: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN,
    soundPlayer: SoundPlayer? = null
) {
    // При перезагрузке сразу показываем экран запуска с автоматическим стартом
    StartupScreen(
        onComplete = onComplete,
        theme = theme,
        autoStart = true,
        soundPlayer = soundPlayer
    )
}
