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
import org.vengeful.citymanager.data.severite.ISeveriteInteractor
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.severite.SeveriteCounts

class NIISViewModel(
    private val severiteInteractor: ISeveriteInteractor,
    private val administrationInteractor: IAdministrationInteractor
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
}


