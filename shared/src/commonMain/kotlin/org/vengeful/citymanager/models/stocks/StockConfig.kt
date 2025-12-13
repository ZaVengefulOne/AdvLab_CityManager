package org.vengeful.citymanager.models.stocks

import kotlinx.serialization.Serializable

@Serializable
data class StockConfig(
    val name: String,
    val averagePrice: Double
)
