package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class BankAccount(
    val id: Int,
    val personId: Int? = null,
    val enterpriseName: String? = null,
    val depositAmount: Double = 0.0,
    val creditAmount: Double = 0.0,
)
