package org.vengeful.citymanager.screens.medic

import androidx.lifecycle.viewModelScope
import org.vengeful.citymanager.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.data.medic.IMedicInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.medicine.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification


class MedicViewModel(
    private val medicInteractor: IMedicInteractor,
    private val personInteractor: IPersonInteractor,
    private val bankInteractor: IBankInteractor,
    private val userInteractor: IUserInteractor,
    private val authManager: AuthManager,
) : BaseViewModel() {

    private val _patients = MutableStateFlow<List<Person>>(emptyList())
    val patients: StateFlow<List<Person>> = _patients.asStateFlow()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _availableAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val availableAccounts: StateFlow<List<BankAccount>> = _availableAccounts.asStateFlow()


    private val _currentPerson = MutableStateFlow<Person?>(null)
    val currentPerson: StateFlow<Person?> = _currentPerson.asStateFlow()

    private val _allPersons = MutableStateFlow<List<Person>>(emptyList())
    val allPersons: StateFlow<List<Person>> = _allPersons.asStateFlow()

    private val _currentMedicalRecord = MutableStateFlow<MedicalRecord?>(null)
    val currentMedicalRecord: StateFlow<MedicalRecord?> = _currentMedicalRecord.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _medicalRecords = MutableStateFlow<Map<Int, MedicalRecord>>(emptyMap())
    val medicalRecords: StateFlow<Map<Int, MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _medicineOrders = MutableStateFlow<List<MedicineOrderNotification>>(emptyList())
    val medicineOrders: StateFlow<List<MedicineOrderNotification>> = _medicineOrders.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPatients()
        loadAllPersons()
        loadMedicines()
        loadAvailableAccounts()
        loadMedicineOrders()
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

    fun loadMedicineOrders() {
        viewModelScope.launch {
            try {
                val orders = medicInteractor.getMedicineOrders()
                _medicineOrders.value = orders
            } catch (e: Exception) {
                println("Error loading medicine orders: ${e.message}")
                e.printStackTrace()
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

    fun loadMedicines() {
        viewModelScope.launch {
            try {
                _medicines.value = medicInteractor.getAllMedicines()
            } catch (e: Exception) {
                println("Error loading medicines: ${e.message}")
            }
        }
    }

    fun loadAvailableAccounts() {
        viewModelScope.launch {
            try {
                val accounts = mutableListOf<BankAccount>()

                // Получаем счет предприятия "Больница"
                try {
                    val medicAccount = bankInteractor.getBankAccountByEnterpriseName("Больница")
                    medicAccount?.let {
                        accounts.add(it)
                        println("Added medic account: ${it.id}, name: ${it.enterpriseName}, balance: ${it.creditAmount}")
                    } ?:
                    run {
                        println("Medic account 'Больница' not found, searching all enterprise accounts...")
                        val allAccounts1 = bankInteractor.getAllBankAccounts()
                        val enterpriseAccounts1 = allAccounts1.filter { it.enterpriseName != null }
                        println("Found ${enterpriseAccounts1.size} enterprise accounts: ${enterpriseAccounts1.map { it.enterpriseName }}")
                        // Попробуем найти по разным вариантам названия
                        val medicAccountVariants1 = enterpriseAccounts1.find {
                            it.enterpriseName?.contains("Больница", ignoreCase = true) == true ||
                                it.enterpriseName?.contains("Медик", ignoreCase = true) == true ||
                                it.enterpriseName?.contains("Hospital", ignoreCase = true) == true
                        }
                        medicAccountVariants1?.let {
                            accounts.add(it)
                            println("Added medic account (found by variant): ${it.id}, name: ${it.enterpriseName}, balance: ${it.creditAmount}")
                        } ?: println("Medic account not found - возможно, счет предприятия 'Больница' не создан")
                    }
                } catch (e: Exception) {
                    println("Error loading medic account: ${e.message}")
                    e.printStackTrace()
                }

                try {
                    val userResponse = userInteractor.getCurrentUserWithPersonId()
                    val personId = userResponse?.personId?.takeIf { it > 0 }
                    if (personId != null) {
                        val person = try {
                            personInteractor.getPersonById(personId)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        _currentPerson.value = person as Person?
                        try {
                            val allAccounts = bankInteractor.getAllBankAccounts()
                            val personalAccount = allAccounts.find { it.personId == personId }
                            personalAccount?.let {
                                accounts.add(it)
                            } ?: println("Personal account not found for personId: $personId")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        _currentPerson.value = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _currentPerson.value = null
                }
                _availableAccounts.value = accounts
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun orderMedicine(medicineId: Int, quantity: Int, accountId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                medicInteractor.orderMedicine(medicineId, quantity, accountId)
                _successMessage.value = "Заказ лекарств успешно оформлен!"
                loadAvailableAccounts()
                kotlinx.coroutines.delay(3000)
                _successMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка заказа лекарств: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicalRecord(recordId: Int) {
        viewModelScope.launch {
            try {
                val success = medicInteractor.deleteMedicalRecord(recordId)
                if (success) {
                    loadPatients() // Обновляем список пациентов
                    clearCurrentMedicalRecord()
                }
            } catch (e: Exception) {
                println("Error deleting medical record: ${e.message}")
                e.printStackTrace()
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
