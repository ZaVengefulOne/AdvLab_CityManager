package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class CreateBankAccountRequest(
    val personId: Int? = null,
    val enterpriseName: String? = null,
    val creditAmount: Double = 0.0,
    val personBalance: Double? = null
)
