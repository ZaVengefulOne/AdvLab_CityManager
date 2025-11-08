package org.vengeful.citymanager.data.users

class AuthManager {
    private var currentToken: String? = null

    fun saveToken(token: String) {
        currentToken = token
        // Здесь можно добавить сохранение в SecurePreferences или Keychain
    }

    fun getToken(): String? = currentToken

    fun clearToken() {
        currentToken = null
    }

    fun isLoggedIn(): Boolean = currentToken != null
}