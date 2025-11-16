package org.vengeful.citymanager.data.users

import org.vengeful.citymanager.models.Rights

class AuthManager {
    private var currentToken: String? = null
    private var currentUsername: String? = null
    private var currentRights: List<Rights> = emptyList()
    private var currentClicks: Int = 0
    private var currentUserId: Int? = null

    fun saveToken(token: String) {
        currentToken = token
    }

    fun saveUserInfo(username: String, rights: List<Rights>, clicks: Int = 0, userId: Int? = null) {
        currentUsername = username
        currentRights = rights
        currentClicks = clicks
        currentUserId = userId
    }

    fun saveClicks(clicks: Int) {
        currentClicks = clicks
    }

    fun getUserId(): Int? = currentUserId
    fun getToken(): String? = currentToken
    fun getUsername(): String? = currentUsername
    fun getRights(): List<Rights> = currentRights
    fun getClicks(): Int = currentClicks

    fun clearToken() {
        currentToken = null
        currentUsername = null
        currentRights = emptyList()
        currentClicks = 0
        currentUserId = null
    }

    fun isLoggedIn(): Boolean = currentToken != null
}