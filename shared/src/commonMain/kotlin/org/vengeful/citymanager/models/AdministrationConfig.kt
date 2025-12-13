package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.stocks.StockConfig

@Serializable
data class AdministrationConfig(
    val severiteRate: Double,
    val controlLossThreshold: Int,
    val recentMessages: List<ChatMessage> = emptyList(),
    val stocks: List<StockConfig> = emptyList()
)
