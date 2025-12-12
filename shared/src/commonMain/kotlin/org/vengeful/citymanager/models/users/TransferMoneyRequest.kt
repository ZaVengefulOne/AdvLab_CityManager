package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class TransferMoneyRequest(
    val fromPersonId: Int,
    val toPersonId: Int,
    val amount: Double
)
