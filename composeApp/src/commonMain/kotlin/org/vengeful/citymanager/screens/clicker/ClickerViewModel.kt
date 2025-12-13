package org.vengeful.citymanager.screens.clicker

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
import kotlin.math.pow

class ClickerViewModel(
    private val userInteractor: IUserInteractor,
    private val authManager: AuthManager,
    private val bankInteractor: IBankInteractor,
    private val personInteractor: IPersonInteractor
): BaseViewModel() {
    private val _ebanatAmount = MutableStateFlow(0)
    val ebanatAmount: StateFlow<Int> = _ebanatAmount.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    private val _hasSaveProgressUpgrade = MutableStateFlow(false)
    val hasSaveProgressUpgrade: StateFlow<Boolean> = _hasSaveProgressUpgrade.asStateFlow()

    private val _currentPersonId = MutableStateFlow<Int?>(null)
    val currentPersonId: StateFlow<Int?> = _currentPersonId.asStateFlow()

    private val _hasBankAccount = MutableStateFlow(false)
    val hasBankAccount: StateFlow<Boolean> = _hasBankAccount.asStateFlow()

    private val _clickMultiplier = MutableStateFlow(1)
    val clickMultiplier: StateFlow<Int> = _clickMultiplier.asStateFlow()

    fun loadClicks() {
        viewModelScope.launch {
            _userId.value = authManager.getUserId()
            val userId = _userId.value

            if (userId != null) {
                // Загружаем информацию о пользователе
                val users = userInteractor.getAllUsers()
                val currentUser = users.find { it.id == userId }

                if (currentUser != null) {
                    _hasSaveProgressUpgrade.value = currentUser.hasSaveProgressUpgrade
                    _currentPersonId.value = currentUser.personId
                    _clickMultiplier.value = currentUser.clickMultiplier  // Загружаем множитель

                    // Проверяем наличие банковского счета
                    if (currentUser.personId != null) {
                        val bankAccount = bankInteractor.getBankAccountByPersonId(currentUser.personId!!)
                        _hasBankAccount.value = bankAccount != null
                    }

                    // Загружаем клики только если есть улучшение
                    if (currentUser.hasSaveProgressUpgrade) {
                        val dbClicks = userInteractor.getCurrentUserClicks()
                        if (dbClicks != null) {
                            _ebanatAmount.value = dbClicks
                            authManager.saveClicks(dbClicks)
                        } else {
                            _ebanatAmount.value = authManager.getClicks()
                        }
                    } else {
                        // По умолчанию сбрасываем клики
                        _ebanatAmount.value = 0
                    }
                } else {
                    _ebanatAmount.value = 0
                }
            } else {
                _ebanatAmount.value = 0
            }
        }
    }

    fun incrementClicks() {
        // Умножаем на множитель
        _ebanatAmount.value += _clickMultiplier.value
    }

    // Вычисляем стоимость следующего уровня множителя
    fun getNextMultiplierUpgradePrice(): Int {
        val currentLevel = _clickMultiplier.value
        // Формула: базовая_цена * (множитель_цены ^ (текущий_уровень - 1))
        return (ClickerConstants.CLICK_MULTIPLIER_BASE_PRICE *
            ClickerConstants.CLICK_MULTIPLIER_PRICE_MULTIPLIER.pow(currentLevel - 1)).toInt()
    }

    fun saveClicks() {
        viewModelScope.launch {
            val userId = _userId.value
            if (userId != null && _hasSaveProgressUpgrade.value) {
                // Сохраняем только если есть улучшение
                try {
                    val success = userInteractor.updateClicks(userId, _ebanatAmount.value)
                    if (success) {
                        authManager.saveClicks(_ebanatAmount.value)
                    }
                } catch (e: Exception) {
                    println("Error saving SeveriteCoin: ${e.message}")
                    e.printStackTrace()
                }
            }
            // Если улучшения нет, клики не сохраняются (сбрасываются при выходе)
        }
    }

    fun purchaseSaveProgressUpgrade(): Boolean {
        return if (_ebanatAmount.value >= ClickerConstants.SAVE_PROGRESS_UPGRADE_PRICE && !_hasSaveProgressUpgrade.value) {
            viewModelScope.launch {
                val userId = _userId.value
                if (userId != null) {
                    try {
                        val success = userInteractor.purchaseSaveProgressUpgrade(userId)
                        if (success) {
                            _ebanatAmount.value -= ClickerConstants.SAVE_PROGRESS_UPGRADE_PRICE
                            _hasSaveProgressUpgrade.value = true
                            // Сохраняем обновленные клики
                            userInteractor.updateClicks(userId, _ebanatAmount.value)
                            authManager.saveClicks(_ebanatAmount.value)
                        }
                    } catch (e: Exception) {
                        println("Error purchasing upgrade: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            true
        } else {
            false
        }
    }

    fun purchaseClickMultiplierUpgrade(): Boolean {
        val upgradePrice = getNextMultiplierUpgradePrice()
        return if (_ebanatAmount.value >= upgradePrice) {
            viewModelScope.launch {
                val userId = _userId.value
                if (userId != null) {
                    try {
                        val success = userInteractor.purchaseClickMultiplierUpgrade(userId)
                        if (success) {
                            _ebanatAmount.value -= upgradePrice
                            _clickMultiplier.value += 1
                            // Сохраняем обновленные клики
                            if (_hasSaveProgressUpgrade.value) {
                                userInteractor.updateClicks(userId, _ebanatAmount.value)
                                authManager.saveClicks(_ebanatAmount.value)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error purchasing click multiplier upgrade: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            true
        } else {
            false
        }
    }

    fun convertClicksToMoney(): Boolean {
        val personId = _currentPersonId.value
        if (personId == null || !_hasBankAccount.value) {
            return false
        }

        val clicksToConvert = _ebanatAmount.value
        if (clicksToConvert < ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE) {
            return false
        }

        viewModelScope.launch {
            try {
                val person = personInteractor.getPersonById(personId)
                if (person != null) {
                    val moneyToAdd = clicksToConvert / ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE
                    val clicksRemaining = clicksToConvert % ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE

                    // Обновляем баланс персоны
                    val updatedPerson = person.copy(balance = person.balance + moneyToAdd)
                    personInteractor.updatePerson(updatedPerson)

                    // Обновляем клики
                    _ebanatAmount.value = clicksRemaining
                    val userId = _userId.value
                    if (userId != null) {
                        userInteractor.updateClicks(userId, clicksRemaining)
                        authManager.saveClicks(clicksRemaining)
                    }
                }
            } catch (e: Exception) {
                println("Error converting clicks to money: ${e.message}")
                e.printStackTrace()
            }
        }
        return true
    }
}
