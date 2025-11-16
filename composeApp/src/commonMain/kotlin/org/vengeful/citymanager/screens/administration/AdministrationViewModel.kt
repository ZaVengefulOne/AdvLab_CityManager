package org.vengeful.citymanager.screens.administration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.IPersonInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.users.models.RegisterResult
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User


class AdministrationViewModel(
    private val personInteractor: IPersonInteractor,
    private val userInteractor: IUserInteractor,
) : BaseViewModel() {

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _curPerson = MutableStateFlow<Person?>(null)
    val curPerson: StateFlow<Person?> = _curPerson.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    fun register(username: String, password: String, personId: Int?) {
        if (_registerState.value is RegisterUiState.Loading) return

        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            when (val result = userInteractor.register(username, password, personId)) {
                is RegisterResult.Success -> {
                    _registerState.value = RegisterUiState.Success
                    getPersons()
                }
                is RegisterResult.Error -> {
                    _registerState.value = RegisterUiState.Error(result.message)
                }
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUiState.Idle
    }

    fun getPersons() {
        viewModelScope.launch {
            val persons = personInteractor.getPersons()
            _persons.value = persons
        }
    }

    fun getPersonById(id: Int) {
        viewModelScope.launch {
            val person = personInteractor.getPersonById(id)
            _curPerson.value = person
        }
    }

    fun getPersonsByRights(rights: Rights) {
        viewModelScope.launch {
            val persons = personInteractor.getPersonsByRights(rights)
            _persons.value = persons
        }
    }

    fun addPerson(person: Person) {
        viewModelScope.launch {
            personInteractor.addPerson(person)
        }
    }

    fun deletePerson(id: Int) {
        viewModelScope.launch {
            personInteractor.deletePerson(id)
        }
    }


    fun getUsers() {
        viewModelScope.launch {
            try {
                val usersList = userInteractor.getAllUsers()
                _users.value = usersList
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error loading users: ${e.message}")
            }
        }
    }

    fun updateUser(user: User, password: String?, personId: Int?) {
        viewModelScope.launch {
            try {
                val success = userInteractor.updateUser(user, password, personId)
                if (success) {
                    getUsers()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error updating user: ${e.message}")
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                val success = userInteractor.deleteUser(id)
                if (success) {
                    getUsers()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error deleting user: ${e.message}")
            }
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            try {
                personInteractor.updatePerson(person)
                getPersons()
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error updating person: ${e.message}")
            }
        }
    }

    fun toggleUserStatus(userId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                val user = _users.value.find { it.id == userId }
                if (user != null) {
                    val updatedUser = User(
                        id = user.id,
                        username = user.username,
                        passwordHash = user.passwordHash,
                        rights = user.rights,
                        isActive = !currentStatus,
                        createdAt = user.createdAt
                    )
                    val success = userInteractor.updateUser(updatedUser, password = null, personId = null)
                    if (success) {
                        getUsers()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}