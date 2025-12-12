package org.vengeful.citymanager.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class CreateMedicineOrderRequest(
    val medicineId: Int,
    val quantity: Int,
    val accountId: Int // ID банковского счета для оплаты
)
