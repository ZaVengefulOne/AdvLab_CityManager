package org.vengeful.citymanager.userService

import java.security.MessageDigest

object PasswordHasher {
    /**
     * Хеширует пароль используя SHA-256
     * В production рекомендуется использовать BCrypt
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Проверяет, соответствует ли пароль хешу
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}