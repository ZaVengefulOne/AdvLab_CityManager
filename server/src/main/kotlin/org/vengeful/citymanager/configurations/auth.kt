package org.vengeful.citymanager.configurations

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Временная функция аутентификации (замените на свою логику) TODO
@OptIn(ExperimentalTime::class)
fun authenticateUser(username: String, password: String): User? {
    // Здесь должна быть ваша реальная логика проверки пользователя
    // Например, проверка в базе данных
    val users = mapOf(
        "user1" to User(
            id = 1,
            username = "VengefulTest",
            passwordHash = "test",
            rights = listOf(Rights.Joker),
            isActive = true,
            createdAt = Clock.System.now(),
        )
    )

    return users[username]?.takeIf { it.passwordHash == password }
}