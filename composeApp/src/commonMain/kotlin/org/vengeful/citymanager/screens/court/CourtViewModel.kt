package org.vengeful.citymanager.screens.court

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.court.IHearingInteractor
import org.vengeful.citymanager.data.police.ICaseInteractor
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus

class CourtViewModel(
    private val hearingInteractor: IHearingInteractor,
    private val caseInteractor: ICaseInteractor
) : BaseViewModel() {

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
}

