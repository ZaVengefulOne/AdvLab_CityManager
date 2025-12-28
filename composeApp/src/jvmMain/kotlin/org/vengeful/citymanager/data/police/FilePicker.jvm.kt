package org.vengeful.citymanager.data.police

import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual class FilePicker {
    actual suspend fun pickImage(): ByteArray? = withContext(Dispatchers.Swing) {
        suspendCancellableCoroutine { continuation ->
            try {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Выберите фото"
                    fileFilter = FileNameExtensionFilter("Изображения (PNG, JPG)", "png", "jpg", "jpeg", "JPEG")
                }

                // Убеждаемся, что диалог вызывается в EDT
                SwingUtilities.invokeLater {
                    try {
                        val result = fileChooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val selectedFile = fileChooser.selectedFile
                            val extension = selectedFile.name.substringAfterLast('.', "").lowercase()
                            if (extension in listOf("png", "jpg", "jpeg", "JPEG")) {
                                val bytes = selectedFile.readBytes()
                                continuation.resume(bytes)
                            } else {
                                continuation.resume(null)
                            }
                        } else {
                            continuation.resume(null)
                        }
                    } catch (e: Exception) {
                        println("Error in file chooser: ${e.message}")
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                }
            } catch (e: Exception) {
                println("Error creating file chooser: ${e.message}")
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }
}

actual fun createFilePicker(): FilePicker = FilePicker()
