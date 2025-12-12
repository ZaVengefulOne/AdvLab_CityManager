package org.vengeful.citymanager.screens.my_bank

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.models.Person

class MyBankViewModel (
    private val personInteractor: IPersonInteractor,
    private val userInteractor: IUserInteractor,
    private val bankInteractor: IBankInteractor,
    private val authManager: AuthManager
) : BaseViewModel() {

    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _currentPerson = MutableStateFlow<Person?>(null)
    val currentPerson: StateFlow<Person?> = _currentPerson.asStateFlow()

    private val _selectedRecipientId = MutableStateFlow<Int?>(null)
    val selectedRecipientId: StateFlow<Int?> = _selectedRecipientId.asStateFlow()

    private val _transferAmount = MutableStateFlow<String>("")
    val transferAmount: StateFlow<String> = _transferAmount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _transferSuccess = MutableStateFlow(false)
    val transferSuccess: StateFlow<Boolean> = _transferSuccess.asStateFlow()

    init {
        loadPersons()  // ИЗМЕНЕНО: сначала загружаем персон, они загрузят текущую персону
    }

    private fun loadCurrentPerson() {
        viewModelScope.launch {
            try {
                val userId = authManager.getUserId()
                if (userId == null) {
                    return@launch
                }

                var personId: Int? = null

                // Способ 1: Пытаемся получить personId из списка пользователей
                try {
                    val users = userInteractor.getAllUsers()
                    val currentUser = users.find { it.id == userId }
                    personId = currentUser?.personId
                } catch (e: Exception) {
                    // Если getAllUsers() не работает (нет токена или ошибка), пробуем другой способ
                    println("Failed to get users: ${e.message}")
                }

                // Способ 2: Если personId не найден, ищем через банковские счета
                // (если у пользователя есть счёт, можно найти его personId через счета)
                if (personId == null) {
                    try {
                        val bankAccounts = bankInteractor.getAllBankAccounts()
                        // Если у пользователя есть счёт, мы можем найти его через другие методы
                        // Но это не идеально, так как нужно знать personId
                    } catch (e: Exception) {
                        println("Failed to get bank accounts: ${e.message}")
                    }
                }

                // Загружаем персону, если personId найден
                if (personId != null) {
                    val person = personInteractor.getPersonById(personId)
                    if (person != null) {
                        _currentPerson.value = person
                    }
                }
            } catch (e: Exception) {
                println("Error loading current person: ${e.message}")
                // Не устанавливаем ошибку, чтобы не мешать работе экрана
            }
        }
    }

    fun loadPersons() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Загружаем всех персон
                val allPersons = personInteractor.getPersons()

                // Загружаем все банковские счета
                val bankAccounts = bankInteractor.getAllBankAccounts()

                // Получаем список personId, у которых есть банковский счёт
                val personIdsWithAccounts = bankAccounts
                    .mapNotNull { it.personId }
                    .toSet()

                // Фильтруем персон: оставляем только тех, у кого есть банковский счёт
                val personsWithAccounts = allPersons.filter { person ->
                    personIdsWithAccounts.contains(person.id)
                }

                _persons.value = personsWithAccounts

                // ИЗМЕНЕНО: Загружаем текущую персону после получения списка персон с счетами
                val userId = authManager.getUserId()
                if (userId != null && _currentPerson.value == null) {
                    // Пытаемся найти personId через getAllUsers
                    try {
                        val users = userInteractor.getAllUsers()
                        val currentUser = users.find { it.id == userId }
                        val personId = currentUser?.personId

                        if (personId != null) {
                            // Ищем персону в отфильтрованном списке персон с банковскими счетами
                            val currentPersonFromList = personsWithAccounts.find { it.id == personId }
                            if (currentPersonFromList != null) {
                                _currentPerson.value = currentPersonFromList
                            } else {
                                // Если персоны нет в отфильтрованном списке, но у неё есть счёт,
                                // загружаем её отдельно
                                val person = personInteractor.getPersonById(personId)
                                if (person != null && personIdsWithAccounts.contains(personId)) {
                                    _currentPerson.value = person
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Failed to load current person from users: ${e.message}")
                        // Если не удалось загрузить через getAllUsers, пробуем найти через банковские счета
                        // Но для этого нужно знать personId, что невозможно без getUser
                    }
                } else if (_currentPerson.value != null) {
                    // Обновляем текущую персону из отфильтрованного списка
                    val updatedPerson = personsWithAccounts.find { it.id == _currentPerson.value?.id }
                    if (updatedPerson != null) {
                        _currentPerson.value = updatedPerson
                    } else {
                        // Если текущая персона исчезла из списка (например, счёт был удалён),
                        // очищаем её
                        _currentPerson.value = null
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки персон: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setCurrentPerson(person: Person) {
        _currentPerson.value = person
    }

    fun setSelectedRecipient(personId: Int) {
        _selectedRecipientId.value = personId
    }

    fun setTransferAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _transferAmount.value = amount
        }
    }

    fun transferMoney() {
        val currentPerson = _currentPerson.value
        val recipientId = _selectedRecipientId.value
        val amount = _transferAmount.value.toDoubleOrNull()

        if (currentPerson == null) {
            _errorMessage.value = "Не выбран отправитель"
            return
        }

        if (recipientId == null) {
            _errorMessage.value = "Не выбран получатель"
            return
        }

        if (amount == null || amount <= 0) {
            _errorMessage.value = "Неверная сумма перевода"
            return
        }

        if (amount > currentPerson.balance) {
            _errorMessage.value = "Недостаточно средств на балансе"
            return
        }

        if (currentPerson.id == recipientId) {
            _errorMessage.value = "Нельзя переводить деньги самому себе"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                personInteractor.transferMoney(currentPerson.id, recipientId, amount)
                _transferSuccess.value = true
                // Обновляем данные
                loadPersons()
                _transferAmount.value = ""
                _selectedRecipientId.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка перевода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _transferSuccess.value = false
    }
}
