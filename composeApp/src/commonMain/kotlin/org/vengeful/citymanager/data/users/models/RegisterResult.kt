package org.vengeful.citymanager.data.users.models

sealed class RegisterResult {
    data object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}