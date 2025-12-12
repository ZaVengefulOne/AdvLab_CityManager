package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class SalaryPaymentRequest(
    val amount: Double
)
