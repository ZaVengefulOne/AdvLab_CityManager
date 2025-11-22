package org.vengeful.citymanager.data.backup

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual fun saveFile(content: String, filename: String, contentType: String) {
    // Создаем диалог выбора файла
    val fileChooser = JFileChooser().apply {
        dialogTitle = "Сохранить игровой бэкап"
        selectedFile = File(filename)

        // Устанавливаем фильтр в зависимости от формата
        when {
            filename.endsWith(".html") -> {
                fileFilter = FileNameExtensionFilter("HTML файлы (*.html)", "html")
            }
            filename.endsWith(".markdown") || filename.endsWith(".md") -> {
                fileFilter = FileNameExtensionFilter("Markdown файлы (*.md, *.markdown)", "md", "markdown")
            }
        }
    }

    // Показываем диалог сохранения
    val result = fileChooser.showSaveDialog(null)

    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile

        // Убеждаемся, что у файла правильное расширение
        val finalFile = when {
            filename.endsWith(".html") && !selectedFile.name.endsWith(".html") -> {
                File(selectedFile.parent, "${selectedFile.name}.html")
            }
            (filename.endsWith(".markdown") || filename.endsWith(".md"))
                    && !selectedFile.name.endsWith(".md")
                    && !selectedFile.name.endsWith(".markdown") -> {
                File(selectedFile.parent, "${selectedFile.name}.md")
            }
            else -> selectedFile
        }

        // Записываем содержимое в файл
        finalFile.writeText(content, Charsets.UTF_8)
    } else {
        throw Exception("Сохранение отменено пользователем")
    }
}