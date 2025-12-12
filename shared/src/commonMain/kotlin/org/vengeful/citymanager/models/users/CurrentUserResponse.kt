package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class CurrentUserResponse(
    val id: Int,
    val username: String,
    val rights: List<String>,
    val isActive: Boolean,
    val personId: Int
)
