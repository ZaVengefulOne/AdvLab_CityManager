package org.vengeful.citymanager.models.emergencyShutdown

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyShutdownResponse(
    val message: String,
    val durationMinutes: Int,
    val allowedUserId: Int
)

@Serializable
data class EmergencyShutdownStatusResponse(
    val isActive: Boolean,
    val remainingTimeSeconds: Long? = null,
)

@Serializable
data class ErrorResponse(
    val error: String
)
