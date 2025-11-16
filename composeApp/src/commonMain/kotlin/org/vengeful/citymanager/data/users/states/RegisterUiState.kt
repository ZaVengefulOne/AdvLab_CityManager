package org.vengeful.citymanager.data.users.states

sealed class RegisterUiState{
    data object Idle: RegisterUiState()
    data object Loading: RegisterUiState()
    data object Success: RegisterUiState()
    data class Error(val message: String): RegisterUiState()
}
