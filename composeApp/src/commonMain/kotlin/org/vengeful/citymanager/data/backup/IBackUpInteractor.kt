package org.vengeful.citymanager.data.backup

interface IBackupInteractor {
    suspend fun downloadGameBackup(format: String)
}