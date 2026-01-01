package org.vengeful.citymanager.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.court.IHearingInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.police.ICaseInteractor
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.Case

class PersonsListViewModel(
    private val personInteractor: IPersonInteractor,
    private val caseInteractor: ICaseInteractor,
    private val hearingInteractor: IHearingInteractor
) : BaseViewModel() {

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _cases = MutableStateFlow<List<Case>>(emptyList())
    val cases: StateFlow<List<Case>> = _cases.asStateFlow()

    private val _hearings = MutableStateFlow<List<Hearing>>(emptyList())
    val hearings: StateFlow<List<Hearing>> = _hearings.asStateFlow()

    fun loadPersons() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _persons.value = personInteractor.getAdminPersons()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки жителей: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCasesAndHearings() {
        viewModelScope.launch {
            try {
                _cases.value = caseInteractor.getAllCases()
                _hearings.value = hearingInteractor.getAllHearings()
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    fun deletePerson(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                personInteractor.deletePerson(id)
                _successMessage.value = "Житель успешно удалён"
                loadPersons()
                kotlinx.coroutines.delay(2000)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                personInteractor.updatePerson(person)
                _successMessage.value = "Житель успешно обновлён"
                loadPersons()
                kotlinx.coroutines.delay(2000)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCasesForPerson(personId: Int): List<Case> {
        return _cases.value.filter { it.suspectPersonId == personId }
    }

    fun getHearingsForPerson(personId: Int): List<Hearing> {
        return _hearings.value.filter { it.plaintiffPersonId == personId }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

