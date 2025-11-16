package org.vengeful.citymanager.screens.clicker

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor

class ClickerViewModel(
    private val userInteractor: IUserInteractor,
    private val authManager: AuthManager
): BaseViewModel() {
    private val _severiteAmount = MutableStateFlow(0)
    val severiteAmount: StateFlow<Int> = _severiteAmount.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()


    fun loadClicks() {
        viewModelScope.launch {
            _userId.value = authManager.getUserId()
            val dbClicks = userInteractor.getCurrentUserClicks()
            if (dbClicks != null) {
                _severiteAmount.value = dbClicks
                authManager.saveClicks(dbClicks)
            } else {
                _severiteAmount.value = authManager.getClicks()
            }
        }
    }

    fun incrementClicks() {
        _severiteAmount.value++
    }

    fun saveClicks() {
        viewModelScope.launch {
            val userId = _userId.value
            println("Saving clicks: userId=$userId, clicks=${_severiteAmount.value}") // Для отладки
            if (userId != null) {
                try {
                    val success = userInteractor.updateClicks(userId, _severiteAmount.value)
                    if (success) {
                        authManager.saveClicks(_severiteAmount.value)
                        println("Clicks saved successfully") // Для отладки
                    } else {
                        println("Failed to save clicks") // Для отладки
                    }
                } catch (e: Exception) {
                    println("Error saving SeveriteCoin: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                println("Cannot save clicks: userId is null") // Для отладки
            }
        }
    }
}