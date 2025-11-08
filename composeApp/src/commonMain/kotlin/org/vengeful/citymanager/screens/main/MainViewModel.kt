package org.vengeful.citymanager.screens.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.users.IUserInteractor


class MainViewModel(private val interactor: IUserInteractor) {

    private val isLogged = MutableStateFlow(false)
    val isLoggedData = isLogged.asStateFlow()

    private val _loginState = MutableStateFlow("")
    val loginState = _loginState.asStateFlow()

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun login(username: String, password: String) {
        _loginState.value = "$username:$password"
        coroutineScope.launch {
            _loginState.value = "Прошёл в корутину"
            isLogged.value = interactor.login(username, password)
        }
    }

    fun logout() {
        coroutineScope.launch {
           isLogged.value = !interactor.logout()
        }
    }

}