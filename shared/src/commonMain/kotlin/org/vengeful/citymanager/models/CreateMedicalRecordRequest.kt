package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateMedicalRecordRequest(
    val record: MedicalRecord,
    val healthStatus: String
)
