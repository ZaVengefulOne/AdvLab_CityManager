package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val message: String,
    val userId: Int,
    val username: String
)

