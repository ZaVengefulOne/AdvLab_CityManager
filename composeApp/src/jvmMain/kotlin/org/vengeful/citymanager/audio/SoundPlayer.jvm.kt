package org.vengeful.citymanager.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class SoundPlayer {
    private var shutdownClip: Clip? = null
    private var startupClip: Clip? = null
    private var clickClip: Clip? = null
    private var systemWorkingClip: Clip? = null
    private var systemShutdownClip: Clip? = null

    private fun createAudioInputStream(inputStream: InputStream): javax.sound.sampled.AudioInputStream? {
        return try {
            // Обертываем в BufferedInputStream для поддержки mark/reset
            val bufferedStream = BufferedInputStream(inputStream)
            bufferedStream.mark(Int.MAX_VALUE) // Помечаем позицию для reset
            AudioSystem.getAudioInputStream(bufferedStream)
        } catch (e: Exception) {
            // Если не получилось, пробуем скопировать в ByteArray
            try {
                val bytes = inputStream.readBytes()
                val byteArrayStream = ByteArrayInputStream(bytes)
                AudioSystem.getAudioInputStream(byteArrayStream)
            } catch (e2: Exception) {
                println("Не удалось создать AudioInputStream: ${e2.message}")
                null
            }
        }
    }

    actual fun playShutdownSound() {
        try {
            shutdownClip?.close()

            // Сначала пробуем загрузить из ресурсов
            val resourceStream = SoundPlayer::class.java.getResourceAsStream("/shutdown.wav")
            val audioInputStream = if (resourceStream != null) {
                println("Загрузка shutdown.wav из ресурсов")
                createAudioInputStream(resourceStream)
            } else {
                // Если не найдено в ресурсах, пробуем из файловой системы
                val soundFile = File("shutdown.wav")
                if (soundFile.exists()) {
                    println("Загрузка shutdown.wav из файловой системы: ${soundFile.absolutePath}")
                    AudioSystem.getAudioInputStream(soundFile)
                } else {
                    println("Файл shutdown.wav не найден ни в ресурсах, ни в файловой системе")
                    return
                }
            }

            if (audioInputStream == null) {
                println("Не удалось создать AudioInputStream для shutdown.wav")
                return
            }

            shutdownClip = AudioSystem.getClip()
            shutdownClip?.open(audioInputStream)

            // Добавляем слушатель для диагностики
            shutdownClip?.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    println("Звук выключения завершен")
                } else if (event.type == LineEvent.Type.START) {
                    println("Звук выключения начал воспроизведение")
                }
            }

            shutdownClip?.start()
            println("Запуск звука выключения")
        } catch (e: Exception) {
            println("Ошибка при воспроизведении звука выключения: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun playStartupSound() {
        try {
            startupClip?.close()

            // Сначала пробуем загрузить из ресурсов
            val resourceStream = SoundPlayer::class.java.getResourceAsStream("/startup.wav")
            val audioInputStream = if (resourceStream != null) {
                println("Загрузка startup.wav из ресурсов")
                createAudioInputStream(resourceStream)
            } else {
                // Если не найдено в ресурсах, пробуем из файловой системы
                val soundFile = File("startup.wav")
                if (soundFile.exists()) {
                    println("Загрузка startup.wav из файловой системы: ${soundFile.absolutePath}")
                    AudioSystem.getAudioInputStream(soundFile)
                } else {
                    println("Файл startup.wav не найден ни в ресурсах, ни в файловой системе")
                    return
                }
            }

            if (audioInputStream == null) {
                println("Не удалось создать AudioInputStream для startup.wav")
                return
            }

            startupClip = AudioSystem.getClip()
            startupClip?.open(audioInputStream)

            // Добавляем слушатель для диагностики
            startupClip?.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    println("Звук включения завершен")
                } else if (event.type == LineEvent.Type.START) {
                    println("Звук включения начал воспроизведение")
                }
            }

            startupClip?.start()
            println("Запуск звука включения")
        } catch (e: Exception) {
            println("Ошибка при воспроизведении звука включения: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun playClickSound() {
        try {
            clickClip?.close()
            val resourceStream = SoundPlayer::class.java.getResourceAsStream("/click.wav")
            val audioInputStream = if (resourceStream != null) {
                createAudioInputStream(resourceStream)
            } else {
                val soundFile = File("click.wav")
                if (soundFile.exists()) {
                    AudioSystem.getAudioInputStream(soundFile)
                } else {
                    return
                }
            }
            if (audioInputStream == null) return
            clickClip = AudioSystem.getClip()
            clickClip?.open(audioInputStream)
            clickClip?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSystemWorkingSound() {
        try {
            systemWorkingClip?.close()
            val resourceStream = SoundPlayer::class.java.getResourceAsStream("/system_working.wav")
            val audioInputStream = if (resourceStream != null) {
                createAudioInputStream(resourceStream)
            } else {
                val soundFile = File("system_working.wav")
                if (soundFile.exists()) {
                    AudioSystem.getAudioInputStream(soundFile)
                } else {
                    return
                }
            }
            if (audioInputStream == null) return
            systemWorkingClip = AudioSystem.getClip()
            systemWorkingClip?.open(audioInputStream)
            systemWorkingClip?.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY) // Зацикливаем звук
            systemWorkingClip?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stopSystemWorkingSound() {
        try {
            systemWorkingClip?.stop()
            systemWorkingClip?.close()
            systemWorkingClip = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSystemShutdownSound() {
        try {
            systemShutdownClip?.close()
            val resourceStream = SoundPlayer::class.java.getResourceAsStream("/system_shutdown.wav")
            val audioInputStream = if (resourceStream != null) {
                createAudioInputStream(resourceStream)
            } else {
                val soundFile = File("system_shutdown.wav")
                if (soundFile.exists()) {
                    AudioSystem.getAudioInputStream(soundFile)
                } else {
                    return
                }
            }
            if (audioInputStream == null) return
            systemShutdownClip = AudioSystem.getClip()
            systemShutdownClip?.open(audioInputStream)
            systemShutdownClip?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun release() {
        shutdownClip?.close()
        startupClip?.close()
        clickClip?.close()
        systemWorkingClip?.close()
        systemShutdownClip?.close()
        shutdownClip = null
        startupClip = null
        clickClip = null
        systemWorkingClip = null
        systemShutdownClip = null
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    return remember { SoundPlayer() }
}
