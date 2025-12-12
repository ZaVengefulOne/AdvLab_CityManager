package org.vengeful.citymanager.models.medicine

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class MedicalRecord @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: Long,
    val workplace: String,
    val doctor: String,
    val prescribedTreatment: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)
