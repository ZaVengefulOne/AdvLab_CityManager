package org.vengeful.citymanager.models.users

import org.vengeful.citymanager.models.Rights

data class AuthResponse(
    val token: String,
    val user: User,
    val availableRights: List<Rights>, // TODO: questionable, переписать
)
