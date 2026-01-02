package org.vengeful.citymanager.data.backup

expect class BackupFilePicker {
    suspend fun pickJsonFile(): String?
}

expect fun createBackupFilePicker(): BackupFilePicker



