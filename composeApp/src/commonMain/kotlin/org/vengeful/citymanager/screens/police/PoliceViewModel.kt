package org.vengeful.citymanager.screens.police

import androidx.lifecycle.viewModelScope
import org.vengeful.citymanager.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.police.IPoliceInteractor
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.PoliceRecord

class PoliceViewModel(
    private val policeInteractor: IPoliceInteractor,
    private val personInteractor: IPersonInteractor,
) : BaseViewModel() {

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _allPersons = MutableStateFlow<List<Person>>(emptyList())
    val allPersons: StateFlow<List<Person>> = _allPersons.asStateFlow()

    private val _currentRecord = MutableStateFlow<PoliceRecord?>(null)
    val currentRecord: StateFlow<PoliceRecord?> = _currentRecord.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _policeRecords = MutableStateFlow<Map<Int, PoliceRecord>>(emptyMap())
    val policeRecords: StateFlow<Map<Int, PoliceRecord>> = _policeRecords.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadPersons()
        loadAllPersons()
    }

    fun loadPersons() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val personsList = policeInteractor.getPersonsWithRecords()
                _persons.value = personsList

                val recordsMap = mutableMapOf<Int, PoliceRecord>()
                personsList.forEach { person ->
                    try {
                        val records = policeInteractor.getPoliceRecordsByPersonId(person.id)
                        records.firstOrNull()?.let { record ->
                            recordsMap[person.id] = record
                        }
                    } catch (e: Exception) {
                        println("Error loading police record for person ${person.id}: ${e.message}")
                    }
                }
                _policeRecords.value = recordsMap
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки личных дел: ${e.message}"
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

    fun loadPoliceRecordByPersonId(personId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val records = policeInteractor.getPoliceRecordsByPersonId(personId)
                _currentRecord.value = records.firstOrNull()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки личного дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPoliceRecord(record: PoliceRecord, photoBytes: ByteArray?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                policeInteractor.createPoliceRecord(record, photoBytes)
                _successMessage.value = "Личное дело успешно создано!"
                loadPersons()
                kotlinx.coroutines.delay(3000)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания личного дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePoliceRecord(recordId: Int, record: PoliceRecord, photoBytes: ByteArray?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                policeInteractor.updatePoliceRecord(recordId, record, photoBytes)
                loadPersons()
                _currentRecord.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления личного дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePoliceRecord(recordId: Int) {
        viewModelScope.launch {
            try {
                val success = policeInteractor.deletePoliceRecord(recordId)
                if (success) {
                    loadPersons()
                    clearCurrentRecord()
                }
            } catch (e: Exception) {
                println("Error deleting police record: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun clearCurrentRecord() {
        _currentRecord.value = null
    }

    fun getPoliceRecordByFingerprintNumber(fingerprintNumber: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val record = policeInteractor.getPoliceRecordByFingerprintNumber(fingerprintNumber)
                _currentRecord.value = record
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки личного дела: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

