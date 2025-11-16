package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights

@Serializable
data class UpdateUserRequest(
    val id: Int,
    val username: String,
    val password: String? = null, // Опциональный пароль (если null - не обновляем)
    val rights: List<Rights>,
    val isActive: Boolean,
    val personId: Int? = null // ID связанного Person (null для отвязки)
)
