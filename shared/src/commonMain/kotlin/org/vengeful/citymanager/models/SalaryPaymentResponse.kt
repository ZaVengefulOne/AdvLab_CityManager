package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class SalaryPaymentResponse(
    val message: String,
    val successCount: Int,
    val failedCount: Int,
    val totalAmount: Double,
    val errors: List<String>
)
