package org.vengeful.citymanager.screens.niis

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.severite.ISeveriteInteractor
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.severite.SeveriteCounts

class NIISViewModel(
    private val severiteInteractor: ISeveriteInteractor,
    private val administrationInteractor: IAdministrationInteractor,
    private val personInteractor: IPersonInteractor
) : BaseViewModel() {

    private val _callStatus = MutableStateFlow<CallStatus?>(null)
    val callStatus: StateFlow<CallStatus?> = _callStatus.asStateFlow()

    private var statusCheckJob: Job? = null

    private val _severiteCounts = MutableStateFlow<SeveriteCounts?>(null)
    val severiteCounts: StateFlow<SeveriteCounts?> = _severiteCounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEmergencyButtonPressed = MutableStateFlow(false)
    val isEmergencyButtonPressed: StateFlow<Boolean> = _isEmergencyButtonPressed.asStateFlow()

    private val _allPersons = MutableStateFlow<List<Person>>(emptyList())
    val allPersons: StateFlow<List<Person>> = _allPersons.asStateFlow()

    fun loadSeveriteCounts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val counts = severiteInteractor.getSeveriteCounts()
                _severiteCounts.value = counts
            } catch (e: Exception) {
                println("Failed to load severite counts: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllPersons() {
        viewModelScope.launch {
            try {
                _allPersons.value = personInteractor.getPersons()
            } catch (e: Exception) {
                println("Error loading all persons: ${e.message}")
            }
        }
    }

    fun startStatusCheck() {
        statusCheckJob?.cancel()
        statusCheckJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                try {
                    val status = administrationInteractor.getCallStatus(Enterprise.NIIS)
                    _callStatus.value = status
                } catch (e: Exception) {
                    println("Error checking call status: ${e.message}")
                }
            }
        }
    }

    fun resetCall() {
        viewModelScope.launch {
            try {
                administrationInteractor.resetCallStatus(Enterprise.NIIS)
                _callStatus.value = CallStatus(Enterprise.NIIS, false)
            } catch (e: Exception) {
                println("Error resetting call: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        statusCheckJob?.cancel()
    }

    fun sendEmergencyAlert() {
        viewModelScope.launch {
            try {
                _isEmergencyButtonPressed.value = true
                val success = administrationInteractor.sendEmergencyAlert(Enterprise.NIIS)
                if (!success) {
                    println("Failed to send emergency alert")
                    _isEmergencyButtonPressed.value = false
                }
            } catch (e: Exception) {
                println("Error sending emergency alert: ${e.message}")
                _isEmergencyButtonPressed.value = false
            }
        }
    }

    fun resetEmergencyButtonState() {
        _isEmergencyButtonPressed.value = false
    }

    fun getPersonnelByRight(right: Rights): List<Person> {
        return _allPersons.value.filter { it.rights.contains(right) }
    }

    fun addRightToPerson(personId: Int, right: Rights) {
        viewModelScope.launch {
            try {
                val person = personInteractor.getPersonById(personId)
                if (person != null && !person.rights.contains(right)) {
                    val updatedRights = person.rights + right
                    val updatedPerson = person.copy(rights = updatedRights)
                    personInteractor.updatePerson(updatedPerson)
                    loadAllPersons()
                }
            } catch (e: Exception) {
                println("Ошибка при найме: ${e.message}")
            }
        }
    }

    fun removeRightFromPerson(personId: Int, right: Rights) {
        viewModelScope.launch {
            try {
                val person = personInteractor.getPersonById(personId)
                if (person != null && person.rights.contains(right)) {
                    val updatedRights = person.rights.filter { it != right }
                    val updatedPerson = person.copy(rights = updatedRights)
                    personInteractor.updatePerson(updatedPerson)
                    loadAllPersons()
                }
            } catch (e: Exception) {
                println("Ошибка при увольнении: ${e.message}")
            }
        }
    }
}


