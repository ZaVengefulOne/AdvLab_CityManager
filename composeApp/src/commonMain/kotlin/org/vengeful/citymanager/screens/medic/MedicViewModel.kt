package org.vengeful.citymanager.screens.medic

import androidx.lifecycle.viewModelScope
import org.vengeful.citymanager.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.medic.IMedicInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.models.MedicalRecord
import org.vengeful.citymanager.models.Person


class MedicViewModel(
    private val medicInteractor: IMedicInteractor,
    private val personInteractor: IPersonInteractor
) : BaseViewModel() {

    private val _patients = MutableStateFlow<List<Person>>(emptyList())
    val patients: StateFlow<List<Person>> = _patients.asStateFlow()

    private val _allPersons = MutableStateFlow<List<Person>>(emptyList())
    val allPersons: StateFlow<List<Person>> = _allPersons.asStateFlow()

    private val _currentMedicalRecord = MutableStateFlow<MedicalRecord?>(null)
    val currentMedicalRecord: StateFlow<MedicalRecord?> = _currentMedicalRecord.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _medicalRecords = MutableStateFlow<Map<Int, MedicalRecord>>(emptyMap())
    val medicalRecords: StateFlow<Map<Int, MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPatients()
        loadAllPersons()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val patientsList = medicInteractor.getPatientsWithRecords()
                _patients.value = patientsList

                val recordsMap = mutableMapOf<Int, MedicalRecord>()
                patientsList.forEach { patient ->
                    try {
                        val records = medicInteractor.getMedicalRecordsByPersonId(patient.id)
                        records.firstOrNull()?.let { record ->
                            recordsMap[patient.id] = record
                        }
                    } catch (e: Exception) {
                        println("Error loading medical record for patient ${patient.id}: ${e.message}")
                    }
                }
                _medicalRecords.value = recordsMap
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки пациентов: ${e.message}"
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

    fun loadMedicalRecordByPersonId(personId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val records = medicInteractor.getMedicalRecordsByPersonId(personId)
                _currentMedicalRecord.value = records.firstOrNull()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки медкарты: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMedicalRecord(recordId: Int, record: MedicalRecord, healthStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                medicInteractor.updateMedicalRecord(recordId, record, healthStatus)
                loadPatients() // Обновляем список после изменения
                _currentMedicalRecord.value = null // Закрываем диалог
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления медкарты: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentMedicalRecord() {
        _currentMedicalRecord.value = null
    }

    fun createMedicalRecord(record: MedicalRecord, healthStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                medicInteractor.createMedicalRecord(record, healthStatus)
                loadPatients()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания медкарты: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
