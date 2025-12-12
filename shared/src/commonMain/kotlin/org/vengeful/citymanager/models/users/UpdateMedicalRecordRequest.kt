package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.medicine.MedicalRecord

@Serializable
data class UpdateMedicalRecordRequest(
    val record: MedicalRecord,
    val healthStatus: String
)
