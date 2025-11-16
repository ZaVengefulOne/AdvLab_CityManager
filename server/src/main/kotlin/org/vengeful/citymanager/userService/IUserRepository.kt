package org.vengeful.citymanager.userService

import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User

interface IUserRepository {
    fun findByUsername(username: String): User?
    fun findById(id: Int): User?
    fun createUser(user: User, password: String): User
    fun updateUser(user: User): User?
    fun deleteUser(id: Int): Boolean
    fun authenticateUser(username: String, password: String): User?
    fun getAllUsers(): List<User>
    fun userExists(username: String): Boolean
    fun updateUser(user: User, password: String?, personId: Int?): User?

    fun registerUser(
        username: String,
        password: String,
        personId: Int?,
        rights: List<Rights> = listOf(Rights.Any)
    ): User

    fun updateUserClicks(userId: Int, severiteClicks: Int): Boolean
}