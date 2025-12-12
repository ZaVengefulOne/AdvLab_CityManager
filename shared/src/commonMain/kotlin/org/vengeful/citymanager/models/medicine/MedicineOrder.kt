package org.vengeful.citymanager.models.medicine

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class MedicineOrder @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val medicineId: Int,
    val medicineName: String,
    val quantity: Int,
    val totalPrice: Double,
    val accountId: Int,
    val orderedByPersonId: Int?,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)
