package org.vengeful.citymanager.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class SoundPlayer(private val context: Context) {
    private var shutdownPlayer: MediaPlayer? = null
    private var startupPlayer: MediaPlayer? = null
    private var clickPlayer: MediaPlayer? = null
    private var systemWorkingPlayer: MediaPlayer? = null
    private var systemShutdownPlayer: MediaPlayer? = null

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

    actual fun playClickSound() {
        try {
            clickPlayer?.release()
            clickPlayer = MediaPlayer.create(context, context.resources.getIdentifier("click", "raw", context.packageName))
            clickPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSystemWorkingSound() {
        try {
            systemWorkingPlayer?.release()
            systemWorkingPlayer = MediaPlayer.create(context, context.resources.getIdentifier("system_working", "raw", context.packageName))
            systemWorkingPlayer?.isLooping = true // Зацикливаем звук
            systemWorkingPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stopSystemWorkingSound() {
        try {
            systemWorkingPlayer?.stop()
            systemWorkingPlayer?.release()
            systemWorkingPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSystemShutdownSound() {
        try {
            systemShutdownPlayer?.release()
            systemShutdownPlayer = MediaPlayer.create(context, context.resources.getIdentifier("system_shutdown", "raw", context.packageName))
            systemShutdownPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun release() {
        shutdownPlayer?.release()
        startupPlayer?.release()
        clickPlayer?.release()
        systemWorkingPlayer?.release()
        systemShutdownPlayer?.release()
        shutdownPlayer = null
        startupPlayer = null
        clickPlayer = null
        systemWorkingPlayer = null
        systemShutdownPlayer = null
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember { SoundPlayer(context) }
}

