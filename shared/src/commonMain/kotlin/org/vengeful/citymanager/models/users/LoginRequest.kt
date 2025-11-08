package org.vengeful.citymanager.models.users

data class LoginRequest(
    val username: String,
    val password: String
)