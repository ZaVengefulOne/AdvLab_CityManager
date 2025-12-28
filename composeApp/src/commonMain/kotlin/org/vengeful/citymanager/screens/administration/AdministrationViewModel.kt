package org.vengeful.citymanager.screens.administration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.data.backup.IBackupInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.severite.ISeveriteInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.data.users.models.RegisterResult
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.models.ChatMessage
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User
import kotlin.random.Random


class AdministrationViewModel(
    private val personInteractor: IPersonInteractor,
    private val userInteractor: IUserInteractor,
    private val administrationInteractor: IAdministrationInteractor,
    private val backupInteractor: IBackupInteractor,
    private val severiteInteractor: ISeveriteInteractor
) : BaseViewModel() {

    private val _severitRate = MutableStateFlow<Double>(42.75)
    val severitRate: StateFlow<Double> = _severitRate.asStateFlow()

    private val _controlLossThreshold = MutableStateFlow<Int>(75)
    val controlLossThreshold: StateFlow<Int> = _controlLossThreshold.asStateFlow()

    private val _severitRateHistory = MutableStateFlow<List<Double>>(emptyList())
    val severitRateHistory: StateFlow<List<Double>> = _severitRateHistory.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

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

    private val _isEmergencyShutdownActive = MutableStateFlow<Boolean>(false)
    val isEmergencyShutdownActive: StateFlow<Boolean> = _isEmergencyShutdownActive.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow<Long?>(null)
    val remainingTimeSeconds: StateFlow<Long?> = _remainingTimeSeconds.asStateFlow()

    private val _severites = MutableStateFlow<List<Severite>>(emptyList())
    val severites: StateFlow<List<Severite>> = _severites.asStateFlow()

    private var emergencyShutdownStatusJob: Job? = null
    private var updateJob: Job? = null
    private var configUpdateJob: Job? = null
    private var baseSeveritRate: Double = 42.75


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

    fun getAdminConfig() {
        viewModelScope.launch {
            try {
                val config = administrationInteractor.getAdministrationConfig()
                baseSeveritRate = config.severiteRate
                _severitRate.value = config.severiteRate
                _controlLossThreshold.value = config.controlLossThreshold
                _chatMessages.value = config.recentMessages

                if (_severitRateHistory.value.isEmpty()) {
                    _severitRateHistory.value = List(GRAPH_HISTORY_SIZE) { config.severiteRate }
                }

                if (updateJob == null || !updateJob!!.isActive) {
                    startAutoUpdate()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error loading admin config: ${e.message}")
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val success = administrationInteractor.sendMessage(text, "desktop")
                if (success) {
                    getAdminConfig()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error sending message: ${e.message}")
            }
        }
    }

    fun startConfigUpdates() {
        configUpdateJob?.cancel()
        configUpdateJob = viewModelScope.launch {
            while (true) {
                delay(CONFIG_UPDATE_INTERVAL_MS)
                getAdminConfig()
            }
        }
    }

    private fun startAutoUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MS)
                val fluctuation = Random.nextDouble(
                    FLUCTUATION_MIN.toDouble(),
                    FLUCTUATION_MAX.toDouble()
                )
                val newRate = baseSeveritRate + fluctuation

                _severitRate.value = newRate

                // Обновляем историю
                val currentHistory = _severitRateHistory.value.toMutableList()
                currentHistory.add(newRate)
                if (currentHistory.size > GRAPH_HISTORY_SIZE) {
                    currentHistory.removeFirst()
                }
                _severitRateHistory.value = currentHistory
            }
        }
    }

    fun callEnterprise(enterprise: Enterprise) {
        viewModelScope.launch {
            try {
                val success = administrationInteractor.callEnterprise(enterprise)
                if (!success) {
                    _errorMessage.value = "Не удалось вызвать представителя"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error calling enterprise: ${e.message}")
            }
        }
    }

    fun activateEmergencyShutdown(durationMinutes: Int, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                val success = administrationInteractor.activateEmergencyShutdown(durationMinutes, password)
                if (success) {
                    _isEmergencyShutdownActive.value = true
                    _errorMessage.value = null
                    checkEmergencyShutdownStatus()
                } else {
                    _errorMessage.value = "Не удалось активировать экстренное отключение"
                }
            } catch (e: Exception) {
                // Сохраняем сообщение об ошибке для отображения в диалоге
                val errorMsg = e.message ?: "Ошибка при активации экстренного отключения"
                _errorMessage.value = errorMsg
                println("Error activating emergency shutdown: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun checkEmergencyShutdownStatus() {
        viewModelScope.launch {
            try {
                val status = administrationInteractor.getEmergencyShutdownStatus()
                _isEmergencyShutdownActive.value = status.isActive
                _remainingTimeSeconds.value = status.remainingTimeSeconds

                if (status.isActive) {
                    startPeriodicStatusSync()
                } else {
                    stopPeriodicStatusSync()
                }
            } catch (e: Exception) {
                println("Error checking emergency shutdown status: ${e.message}")
            }
        }
    }

    fun downloadGameBackup(format: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                backupInteractor.downloadGameBackup(format)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } 
        }
    }

    private fun startPeriodicStatusSync() {
        emergencyShutdownStatusJob?.cancel()
        emergencyShutdownStatusJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Обновляем каждую секунду
                try {
                    val status = administrationInteractor.getEmergencyShutdownStatus()
                    _isEmergencyShutdownActive.value = status.isActive
                    _remainingTimeSeconds.value = status.remainingTimeSeconds

                    if (!status.isActive) {
                        stopPeriodicStatusSync()
                        break
                    }
                } catch (e: Exception) {
                    println("Error syncing emergency shutdown status: ${e.message}")
                }
            }
        }
    }

    private fun stopPeriodicStatusSync() {
        emergencyShutdownStatusJob?.cancel()
        emergencyShutdownStatusJob = null
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
        configUpdateJob?.cancel()
        stopPeriodicStatusSync()
    }

    fun loadSeverites() {
        viewModelScope.launch {
            try {
                val severites = severiteInteractor.getAllSeverite()
                _severites.value = severites
            } catch (e: Exception) {
                println("Failed to load severites: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun sellSeverite(severiteIds: List<Int>) {
        viewModelScope.launch {
            try {
                val result = severiteInteractor.sellSeverite(severiteIds)
                // Обновляем список северита после продажи
                loadSeverites()
                // Обновляем банковские счета (деньги уже добавлены на сервере)
                // Можно добавить обновление банковских счетов здесь, если нужно
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка продажи северита: ${e.message}"
                println("Failed to sell severite: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val UPDATE_INTERVAL_MS = 2000L // 2 секунды
        const val CONFIG_UPDATE_INTERVAL_MS = 15000L // 15 секунд

        const val FLUCTUATION_MIN = -5
        const val FLUCTUATION_MAX = 5
        const val GRAPH_HISTORY_SIZE = 20
    }

}
