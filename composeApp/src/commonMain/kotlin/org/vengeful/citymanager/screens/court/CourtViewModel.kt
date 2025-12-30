package org.vengeful.citymanager.screens.court

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.data.court.IHearingInteractor
import org.vengeful.citymanager.data.police.ICaseInteractor
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus

class CourtViewModel(
    private val hearingInteractor: IHearingInteractor,
    private val caseInteractor: ICaseInteractor,
    private val administrationInteractor: IAdministrationInteractor
) : BaseViewModel() {

    private val _callStatus = MutableStateFlow<CallStatus?>(null)
    val callStatus: StateFlow<CallStatus?> = _callStatus.asStateFlow()

    private var statusCheckJob: Job? = null

    private val _hearings = MutableStateFlow<List<Hearing>>(emptyList())
    val hearings: StateFlow<List<Hearing>> = _hearings.asStateFlow()

    private val _cases = MutableStateFlow<List<Case>>(emptyList())
    val cases: StateFlow<List<Case>> = _cases.asStateFlow()

    private val _casesSentToCourt = MutableStateFlow<List<Case>>(emptyList())
    val casesSentToCourt: StateFlow<List<Case>> = _casesSentToCourt.asStateFlow()

    private val _currentHearing = MutableStateFlow<Hearing?>(null)
    val currentHearing: StateFlow<Hearing?> = _currentHearing.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isEmergencyButtonPressed = MutableStateFlow(false)
    val isEmergencyButtonPressed: StateFlow<Boolean> = _isEmergencyButtonPressed.asStateFlow()

    init {
        loadAllHearings()
        loadAllCases()
    }

    fun loadAllHearings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _hearings.value = hearingInteractor.getAllHearings()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки слушаний: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllCases() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val allCases = caseInteractor.getAllCases()
                _cases.value = allCases
                _casesSentToCourt.value = allCases.filter { it.status == CaseStatus.SENT_TO_COURT }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки дел: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHearingById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentHearing.value = hearingInteractor.getHearingById(id)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки слушания: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createHearing(hearing: Hearing) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val createdHearing = hearingInteractor.createHearing(hearing)
                _successMessage.value = "Слушание успешно создано!"
                // Небольшая задержка перед обновлением списка, чтобы сервер успел обработать запрос
                kotlinx.coroutines.delay(500)
                loadAllHearings()
                loadAllCases()
                kotlinx.coroutines.delay(2500)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания слушания: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateHearing(id: Int, hearing: Hearing) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val updatedHearing = hearingInteractor.updateHearing(id, hearing)
                _currentHearing.value = updatedHearing
                loadAllHearings()
                loadAllCases()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления слушания: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCaseStatus(caseId: Int, status: CaseStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                caseInteractor.updateCaseStatus(caseId, status)
                loadAllCases()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления статуса дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentHearing() {
        _currentHearing.value = null
    }

    fun startStatusCheck() {
        statusCheckJob?.cancel()
        statusCheckJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                try {
                    val status = administrationInteractor.getCallStatus(Enterprise.COURT)
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
                administrationInteractor.resetCallStatus(Enterprise.COURT)
                _callStatus.value = CallStatus(Enterprise.COURT, false)
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
                val success = administrationInteractor.sendEmergencyAlert(Enterprise.COURT)
                if (!success) {
                    _errorMessage.value = "Не удалось отправить тревожное уведомление"
                    _isEmergencyButtonPressed.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isEmergencyButtonPressed.value = false
                println("Error sending emergency alert: ${e.message}")
            }
        }
    }

    fun resetEmergencyButtonState() {
        _isEmergencyButtonPressed.value = false
    }
}

