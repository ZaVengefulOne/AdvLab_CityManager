package org.vengeful.citymanager.models.backup

import kotlinx.serialization.Serializable

@Serializable
data class LimitedMasterBackup(
    val persons: List<MasterBackupPerson>,
    val users: List<MasterBackupUser>,
    val rights: List<MasterBackupRight>,
    val personRights: List<MasterBackupPersonRight>,
    val userRights: List<MasterBackupUserRight>,
    val createdAt: Long
)

