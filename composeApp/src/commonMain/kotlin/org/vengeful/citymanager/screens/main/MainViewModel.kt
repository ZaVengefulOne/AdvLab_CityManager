package org.vengeful.citymanager.screens.main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.ROUTE_ADMINISTRATION
import org.vengeful.citymanager.ROUTE_BANK
import org.vengeful.citymanager.ROUTE_COMMON_LIBRARY
import org.vengeful.citymanager.ROUTE_COURT
import org.vengeful.citymanager.ROUTE_MEDIC
import org.vengeful.citymanager.ROUTE_POLICE
import org.vengeful.citymanager.ROUTE_CLICKER
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.users.models.LoginResult
import org.vengeful.citymanager.data.users.states.LoginUiState
import org.vengeful.citymanager.models.Rights


class MainViewModel(
    private val interactor: IUserInteractor,
    private val authManager: AuthManager
) : BaseViewModel() {

    private val _isLogged = MutableStateFlow(authManager.isLoggedIn())
    val isLoggedData = _isLogged.asStateFlow()

    private val _username = MutableStateFlow(authManager.getUsername() ?: "")
    val username = _username.asStateFlow()

    private val _rights = MutableStateFlow(authManager.getRights())
    val rights = _rights.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState = _loginState.asStateFlow()

    init {
        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        _isLogged.value = authManager.isLoggedIn()
        _username.value = authManager.getUsername() ?: ""
        _rights.value = authManager.getRights()
    }

    fun login(username: String, password: String) {
        if (_loginState.value is LoginUiState.Loading) return

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            when (val result = interactor.login(username, password)) {
                is LoginResult.Success -> {
                    _isLogged.value = true
                    _username.value = authManager.getUsername() ?: ""
                    _rights.value = authManager.getRights()
                    _loginState.value = LoginUiState.Success
                }

                is LoginResult.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val success = interactor.logout()
            if (success) {
                _isLogged.value = false
                _username.value = ""
                _rights.value = emptyList()
                _loginState.value = LoginUiState.Idle
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun hasAccessToScreen(route: String): Boolean {
        val userRights = _rights.value
        val isLogged = _isLogged.value

        if (!isLogged) {
            return route == ROUTE_COMMON_LIBRARY
        }

        if (userRights.contains(Rights.Joker)) {
            return true
        }

        return when (route) {
            ROUTE_ADMINISTRATION -> userRights.contains(Rights.Administration)
            ROUTE_COURT -> userRights.contains(Rights.Court)
            ROUTE_COMMON_LIBRARY -> userRights.contains(Rights.Any)
            ROUTE_MEDIC -> userRights.contains(Rights.Medic)
            ROUTE_POLICE -> userRights.contains(Rights.Police)
            ROUTE_BANK -> userRights.contains(Rights.Bank)
            ROUTE_CLICKER -> userRights.contains(Rights.Any) && isLogged
            else -> false
        }
    }
}