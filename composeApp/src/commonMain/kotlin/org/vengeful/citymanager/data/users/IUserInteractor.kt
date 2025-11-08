package org.vengeful.citymanager.data.users

interface IUserInteractor {
    suspend fun login(username: String, password: String): Boolean
    suspend fun logout(): Boolean
}