package org.vengeful.citymanager.screens.userManagement

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.users.models.RegisterResult
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

class UserManagementViewModel(
    private val personInteractor: IPersonInteractor,
    private val userInteractor: IUserInteractor,
) : BaseViewModel() {

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadPersons() {
        viewModelScope.launch {
            try {
                // Используем публичный эндпоинт для получения списка жителей
                _persons.value = personInteractor.getAdminPersons()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки списка жителей: ${e.message}"
            }
        }
    }

    fun registerUser(
        username: String,
        password: String,
        personId: Int?,
        rights: List<Rights>
    ) {
        if (_registerState.value is RegisterUiState.Loading) return

        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            _errorMessage.value = null

            when (val result = userInteractor.adminRegister(username, password, personId, rights)) {
                is RegisterResult.Success -> {
                    _registerState.value = RegisterUiState.Success
                }
                is RegisterResult.Error -> {
                    _registerState.value = RegisterUiState.Error(result.message)
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUiState.Idle
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
