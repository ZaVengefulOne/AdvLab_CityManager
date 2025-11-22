package org.vengeful.cityManager

import kotlinx.browser.localStorage
import kotlinx.browser.window

class AuthManager {
    private val tokenKey = "admin_token"

    fun saveToken(token: String) {
        localStorage.setItem(tokenKey, token)
    }

    fun getToken(): String? {
        return localStorage.getItem(tokenKey)
    }

    fun clearToken() {
        localStorage.removeItem(tokenKey)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}