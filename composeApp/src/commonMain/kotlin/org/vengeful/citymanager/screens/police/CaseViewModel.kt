package org.vengeful.citymanager.screens.police

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.police.ICaseInteractor
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus

class CaseViewModel(
    private val caseInteractor: ICaseInteractor
) : BaseViewModel() {

    private val _cases = MutableStateFlow<List<Case>>(emptyList())
    val cases: StateFlow<List<Case>> = _cases.asStateFlow()

    private val _currentCase = MutableStateFlow<Case?>(null)
    val currentCase: StateFlow<Case?> = _currentCase.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadAllCases()
    }

    fun loadAllCases() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _cases.value = caseInteractor.getAllCases()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки дел: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCaseById(caseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentCase.value = caseInteractor.getCaseById(caseId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCasesBySuspect(personId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _cases.value = caseInteractor.getCasesBySuspect(personId)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки дел: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCase(case: Case) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val createdCase = caseInteractor.createCase(case)
                _successMessage.value = "Дело успешно создано!"
                loadAllCases()
                kotlinx.coroutines.delay(3000)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCase(caseId: Int, case: Case) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val updatedCase = caseInteractor.updateCase(caseId, case)
                _currentCase.value = updatedCase
                loadAllCases()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления дела: ${e.message}"
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
                val updatedCase = caseInteractor.updateCaseStatus(caseId, status)
                _currentCase.value = updatedCase
                loadAllCases()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления статуса дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCase(caseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = caseInteractor.deleteCase(caseId)
                if (success) {
                    _successMessage.value = "Дело успешно удалено!"
                    loadAllCases()
                    clearCurrentCase()
                    kotlinx.coroutines.delay(3000)
                    _successMessage.value = null
                } else {
                    _errorMessage.value = "Не удалось удалить дело"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentCase() {
        _currentCase.value = null
    }
}

