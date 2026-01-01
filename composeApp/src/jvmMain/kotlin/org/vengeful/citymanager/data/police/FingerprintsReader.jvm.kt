package org.vengeful.citymanager.data.police

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class FingerprintsReader {
    private val fingerprintsDir: File by lazy {
        // Папка fingerprints рядом с исполняемым файлом
        val executablePath = System.getProperty("user.dir")
        File(executablePath, "fingerprints")
    }

    actual fun getAvailableFingerprintNumbers(): List<Int> {
        val dir = fingerprintsDir
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        return dir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in listOf("png", "jpg", "jpeg") }
            ?.mapNotNull { file ->
                val nameWithoutExt = file.nameWithoutExtension
                nameWithoutExt.toIntOrNull()
            }
            ?.sorted()
            ?: emptyList()
    }

    actual fun getNextAvailableFingerprintNumber(): Int? {
        val available = getAvailableFingerprintNumbers()
        if (available.isEmpty()) {
            return 1
        }
        val max = available.maxOrNull() ?: 0
        return max + 1
    }

    actual suspend fun loadFingerprintImage(number: Int): ByteArray? = withContext(Dispatchers.IO) {
        val dir = fingerprintsDir
        if (!dir.exists() || !dir.isDirectory) {
            return@withContext null
        }

        // Пробуем найти файл с таким номером
        val extensions = listOf("png", "jpg", "jpeg")
        for (ext in extensions) {
            val file = File(dir, "$number.$ext")
            if (file.exists() && file.isFile) {
                return@withContext file.readBytes()
            }
        }
        null
    }

    actual fun getAllFingerprintNumbers(): List<Int> {
        return getAvailableFingerprintNumbers()
    }
}

actual fun createFingerprintsReader(): FingerprintsReader = FingerprintsReader()


