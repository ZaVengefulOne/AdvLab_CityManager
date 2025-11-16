package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
    val availableRights: List<Rights>, // TODO: questionable, переписать
)
