package org.vengeful.citymanager.models.stocks

import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val id: Int = 0,
    val name: String,
    val averagePrice: Double,
)
