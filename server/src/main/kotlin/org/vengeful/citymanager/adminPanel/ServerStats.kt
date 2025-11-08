package org.vengeful.citymanager.adminPanel

import kotlinx.serialization.Serializable

@Serializable
data class ServerStats(
    val personCount: Int,
    val activeConnections: Int,
    val uptime: String,
    val memoryUsage: String
)
