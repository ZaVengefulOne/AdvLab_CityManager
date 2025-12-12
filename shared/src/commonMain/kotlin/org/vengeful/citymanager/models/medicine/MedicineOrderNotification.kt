package org.vengeful.citymanager.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class MedicineOrderNotification(
    val id: Int = 0,
    val medicineName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderedByPersonName: String? = null,
    val orderedByEnterprise: String? = null,
    val timestamp: Long,
    val status: String = "pending"
)
