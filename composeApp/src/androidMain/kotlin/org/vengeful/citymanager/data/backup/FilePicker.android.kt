package org.vengeful.citymanager.data.backup

actual class BackupFilePicker {
    actual suspend fun pickJsonFile(): String? {
        // На Android функционал выбора файлов не реализован
        // Можно добавить реализацию через Android Storage Access Framework при необходимости
        throw UnsupportedOperationException("Выбор файлов поддерживается только на desktop")
    }
}

actual fun createBackupFilePicker(): BackupFilePicker = BackupFilePicker()


