package org.vengeful.citymanager.data.backup

import org.vengeful.citymanager.models.backup.LimitedMasterBackup

interface IBackupInteractor {
    suspend fun downloadGameBackup(format: String)
    suspend fun downloadLimitedMasterBackup()
    suspend fun uploadLimitedMasterBackup(backup: LimitedMasterBackup)
}