package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class AdministrationConfig(
    val severiteRate: Double,
    val controlLossThreshold: Int,
    val recentMessages: List<ChatMessage> = emptyList()
)
