package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val text: String,
    val sender: String
)
