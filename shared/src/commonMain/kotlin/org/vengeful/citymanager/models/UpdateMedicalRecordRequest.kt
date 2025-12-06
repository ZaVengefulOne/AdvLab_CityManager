package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMedicalRecordRequest(
    val record: MedicalRecord,
    val healthStatus: String
)
