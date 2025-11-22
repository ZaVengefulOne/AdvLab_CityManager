package org.vengeful.citymanager.models.backup

import kotlinx.serialization.Serializable

@Serializable
data class MasterBackupPerson(
    val id: Int,
    val firstName: String,
    val lastName: String
)

@Serializable
data class MasterBackupRight(
    val id: Int,
    val name: String
)

@Serializable
data class MasterBackupPersonRight(
    val personId: Int,
    val rightId: Int
)

@Serializable
data class MasterBackupUser(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val isActive: Boolean,
    val createdAt: Long,
    val personId: Int?,
    val severiteClicks: Int
)

@Serializable
data class MasterBackupUserRight(
    val userId: Int,
    val rightId: Int
)

@Serializable
data class MasterBackupBankAccount(
    val id: Int,
    val personId: Int?,
    val enterpriseName: String?,
    val depositAmount: Double,
    val creditAmount: Double
)

@Serializable
data class MasterBackup(
    val persons: List<MasterBackupPerson>,
    val rights: List<MasterBackupRight>,
    val personRights: List<MasterBackupPersonRight>,
    val users: List<MasterBackupUser>,
    val userRights: List<MasterBackupUserRight>,
    val bankAccounts: List<MasterBackupBankAccount>,
    val createdAt: Long
)