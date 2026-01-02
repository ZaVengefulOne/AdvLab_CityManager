package org.vengeful.citymanager.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.models.users.User

class UsersListViewModel(
    private val userInteractor: IUserInteractor
) : BaseViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _users.value = userInteractor.getAllUsers()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки пользователей: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = userInteractor.deleteUser(id)
                if (success) {
                    _successMessage.value = "Пользователь успешно удалён"
                    loadUsers()
                    kotlinx.coroutines.delay(2000)
                    _successMessage.value = null
                } else {
                    _errorMessage.value = "Не удалось удалить пользователя"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(user: User, password: String?, personId: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = userInteractor.updateUser(user, password, personId)
                if (success) {
                    _successMessage.value = "Пользователь успешно обновлён"
                    loadUsers()
                    kotlinx.coroutines.delay(2000)
                    _successMessage.value = null
                } else {
                    _errorMessage.value = "Не удалось обновить пользователя"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}


