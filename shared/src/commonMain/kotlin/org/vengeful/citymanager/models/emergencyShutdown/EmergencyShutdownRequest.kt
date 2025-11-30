package org.vengeful.citymanager.models.emergencyShutdown

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyShutdownRequest(
    val durationMinutes: Int,
    val password: String
)


