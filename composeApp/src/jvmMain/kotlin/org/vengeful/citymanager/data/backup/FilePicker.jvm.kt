package org.vengeful.citymanager.data.backup

import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual class BackupFilePicker {
    actual suspend fun pickJsonFile(): String? = withContext(Dispatchers.Swing) {
        suspendCancellableCoroutine { continuation ->
            try {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Выберите файл бэкапа"
                    fileFilter = FileNameExtensionFilter("JSON файлы (*.json)", "json")
                }

                // Убеждаемся, что диалог вызывается в EDT
                SwingUtilities.invokeLater {
                    try {
                        val result = fileChooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val selectedFile = fileChooser.selectedFile
                            val extension = selectedFile.name.substringAfterLast('.', "").lowercase()
                            if (extension == "json") {
                                val content = selectedFile.readText(Charsets.UTF_8)
                                continuation.resume(content)
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

actual fun createBackupFilePicker(): BackupFilePicker = BackupFilePicker()


