package org.vengeful.citymanager.data.users

import org.vengeful.citymanager.data.users.models.LoginResult
import org.vengeful.citymanager.data.users.models.RegisterResult
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.RegisterRequest
import org.vengeful.citymanager.models.users.User

interface IUserInteractor {
    suspend fun login(username: String, password: String): LoginResult
    suspend fun logout(): Boolean
    suspend fun register(username: String, password: String, personId: Int?, rights: List<Rights> = emptyList()): RegisterResult

    suspend fun getAllUsers(): List<User>
    suspend fun updateUser(user: User, password: String?, personId: Int?): Boolean
    suspend fun deleteUser(id: Int): Boolean
    suspend fun updateClicks(userId: Int, clicks: Int): Boolean
    suspend fun getCurrentUserClicks(): Int?

    suspend fun adminRegister(username: String, password: String, personId: Int?, rights: List<Rights>): RegisterResult
}
