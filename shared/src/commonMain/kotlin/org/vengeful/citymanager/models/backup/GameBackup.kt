package org.vengeful.citymanager.models.backup

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights

@Serializable
data class GameBackupPerson(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val health: String = "здоров",
    val balance: Double = 0.0,
    val rights: List<String>
)

@Serializable
data class GameBackupUser(
    val id: Int,
    val username: String,
    val clicks: Int,
    val rights: List<String>
)

@Serializable
data class GameBackupBankAccount(
    val personId: Int?,
    val enterpriseName: String?,
    val creditAmount: Double
)

@Serializable
data class GameBackupEntry(
    val person: GameBackupPerson?,
    val user: GameBackupUser?,
    val bankAccount: GameBackupBankAccount?
)

@Serializable
data class GameBackup(
    val entries: List<GameBackupEntry>,
    val createdAt: Long
)
