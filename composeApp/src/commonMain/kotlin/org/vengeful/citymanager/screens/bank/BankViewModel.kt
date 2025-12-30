package org.vengeful.citymanager.screens.bank

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.administration.AdministrationInteractor
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.users.User

class BankViewModel(
    private val personInteractor: IPersonInteractor,
    private val userInteractor: IUserInteractor,
    private val bankInteractor: IBankInteractor,
    private val administrationInteractor: IAdministrationInteractor
) : BaseViewModel() {

    private val _callStatus = MutableStateFlow<CallStatus?>(null)
    val callStatus: StateFlow<CallStatus?> = _callStatus.asStateFlow()

    private var statusCheckJob: Job? = null

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isEmergencyButtonPressed = MutableStateFlow(false)
    val isEmergencyButtonPressed: StateFlow<Boolean> = _isEmergencyButtonPressed.asStateFlow()

    fun startStatusCheck() {
        statusCheckJob?.cancel()
        statusCheckJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                try {
                    val status = administrationInteractor.getCallStatus(Enterprise.BANK)
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
                administrationInteractor.resetCallStatus(Enterprise.BANK)
                _callStatus.value = CallStatus(Enterprise.BANK, false)
            } catch (e: Exception) {
                println("Error resetting call: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        statusCheckJob?.cancel()
    }

    fun getPersons() {
        viewModelScope.launch {
            try {
                val personsList = personInteractor.getPersons()
                _persons.value = personsList
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error loading persons: ${e.message}")
            }
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

    fun getBankAccounts() {
        viewModelScope.launch {
            try {
                val accountsList = bankInteractor.getAllBankAccounts()
                _bankAccounts.value = accountsList
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error loading bank accounts: ${e.message}")
            }
        }
    }

    fun createBankAccount(
        personId: Int?,
        enterpriseName: String?,
        creditAmount: Double,
        personBalance: Double? = null
    ) {
        viewModelScope.launch {
            try {
                bankInteractor.createBankAccount(personId, enterpriseName, creditAmount, personBalance)
                getBankAccounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun closeCredit(accountId: Int) {
        viewModelScope.launch {
            try {
                bankInteractor.closeCredit(accountId)
                getBankAccounts()
                getPersons()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateBankAccount(bankAccount: BankAccount, personBalance: Double? = null) {
        viewModelScope.launch {
            try {
                val success = bankInteractor.updateBankAccount(bankAccount, personBalance)
                if (success) {
                    getBankAccounts()
                    getPersons()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error updating bank account: ${e.message}")
            }
        }
    }

    fun deleteBankAccount(id: Int) {
        viewModelScope.launch {
            try {
                val success = bankInteractor.deleteBankAccount(id)
                if (success) {
                    getBankAccounts() // Обновляем список
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                println("Error deleting bank account: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun sendEmergencyAlert() {
        viewModelScope.launch {
            try {
                _isEmergencyButtonPressed.value = true
                val success = administrationInteractor.sendEmergencyAlert(Enterprise.BANK)
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
