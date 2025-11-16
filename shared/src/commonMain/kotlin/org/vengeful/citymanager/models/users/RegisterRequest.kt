package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val personId: Int?
)
