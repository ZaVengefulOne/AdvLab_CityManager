package org.vengeful.citymanager.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class SoundPlayer(private val context: Context) {
    private var shutdownPlayer: MediaPlayer? = null
    private var startupPlayer: MediaPlayer? = null

    actual fun playShutdownSound() {
        try {
            shutdownPlayer?.release()
            shutdownPlayer = MediaPlayer.create(context, context.resources.getIdentifier("shutdown", "raw", context.packageName))
            shutdownPlayer?.start()
        } catch (e: Exception) {
            // Если файл не найден, просто игнорируем
            e.printStackTrace()
        }
    }

    actual fun playStartupSound() {
        try {
            startupPlayer?.release()
            startupPlayer = MediaPlayer.create(context, context.resources.getIdentifier("startup", "raw", context.packageName))
            startupPlayer?.start()
        } catch (e: Exception) {
            // Если файл не найден, просто игнорируем
            e.printStackTrace()
        }
    }

    actual fun release() {
        shutdownPlayer?.release()
        startupPlayer?.release()
        shutdownPlayer = null
        startupPlayer = null
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember { SoundPlayer(context) }
}

