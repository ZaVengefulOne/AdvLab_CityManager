package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable


@Serializable
data class UpdateBankAccountRequest(
    val id: Int,
    val personId: Int?,
    val enterpriseName: String? = null,
    val creditAmount: Double,
    val personBalance: Double? = null
)
