package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val personId: Int?,
    val rights: List<Rights> = emptyList()
)
