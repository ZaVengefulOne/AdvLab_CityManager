package org.vengeful.citymanager.adminPanel

import kotlinx.serialization.Serializable

@Serializable
data class RequestLog(
    val timestamp: String,
    val method: String,
    val endpoint: String,
    val status: Int
)
