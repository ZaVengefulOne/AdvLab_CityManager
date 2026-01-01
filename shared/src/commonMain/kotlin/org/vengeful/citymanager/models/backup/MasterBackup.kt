package org.vengeful.citymanager.models.backup

import kotlinx.serialization.Serializable

@Serializable
data class MasterBackupPerson(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val registrationPlace: String = "",
    val health: String = "здоров",
    val balance: Double = 0.0
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
    val severiteClicks: Int,
    val hasSaveProgressUpgrade: Boolean = false,
    val clickMultiplier: Int = 1
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
    val creditAmount: Double
)

@Serializable
data class MasterBackupMedicalRecord(
    val id: Int,
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: Long,
    val workplace: String,
    val doctor: String,
    val prescribedTreatment: String = "",
    val createdAt: Long
)

@Serializable
data class MasterBackupMedicine(
    val id: Int,
    val name: String,
    val price: Double
)

@Serializable
data class MasterBackupMedicineOrder(
    val id: Int,
    val medicineId: Int,
    val medicineName: String,
    val quantity: Int,
    val totalPrice: Double,
    val accountId: Int,
    val orderedByPersonId: Int?,
    val createdAt: Long
)

@Serializable
data class MasterBackupPoliceRecord(
    val id: Int,
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,
    val workplace: String,
    val photoUrl: String?,
    val fingerprintNumber: Int?,
    val createdAt: Long
)

@Serializable
data class MasterBackupCase(
    val id: Int,
    val complainantPersonId: Int?,
    val complainantName: String,
    val investigatorPersonId: Int,
    val suspectPersonId: Int?,
    val suspectName: String,
    val statementText: String,
    val violationArticle: String,
    val status: String,
    val photoCompositeUrl: String?,
    val createdAt: Long
)

@Serializable
data class MasterBackupHearing(
    val id: Int,
    val caseId: Int,
    val plaintiffPersonId: Int?,
    val plaintiffName: String,
    val protocol: String,
    val verdict: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class MasterBackupStock(
    val id: Int,
    val name: String,
    val averagePrice: Double
)

@Serializable
data class MasterBackupArticle(
    val id: Int,
    val title: String,
    val content: String
)

@Serializable
data class MasterBackupNews(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val sourceType: String
)

@Serializable
data class MasterBackupSeverite(
    val id: Int,
    val purity: String,
    val createdAt: Long
)

@Serializable
data class MasterBackup(
    val persons: List<MasterBackupPerson>,
    val rights: List<MasterBackupRight>,
    val personRights: List<MasterBackupPersonRight>,
    val users: List<MasterBackupUser>,
    val userRights: List<MasterBackupUserRight>,
    val bankAccounts: List<MasterBackupBankAccount>,
    val medicalRecords: List<MasterBackupMedicalRecord> = emptyList(),
    val medicines: List<MasterBackupMedicine> = emptyList(),
    val medicineOrders: List<MasterBackupMedicineOrder> = emptyList(),
    val policeRecords: List<MasterBackupPoliceRecord> = emptyList(),
    val cases: List<MasterBackupCase> = emptyList(),
    val hearings: List<MasterBackupHearing> = emptyList(),
    val stocks: List<MasterBackupStock> = emptyList(),
    val articles: List<MasterBackupArticle> = emptyList(),
    val news: List<MasterBackupNews> = emptyList(),
    val severites: List<MasterBackupSeverite> = emptyList(),
    val createdAt: Long
)
