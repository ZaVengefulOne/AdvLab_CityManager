package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val text: String,
    val timestamp: Long,
    val sender: String
)
