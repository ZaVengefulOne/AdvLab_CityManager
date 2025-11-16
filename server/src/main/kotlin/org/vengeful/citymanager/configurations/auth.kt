package org.vengeful.citymanager.configurations

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.userService.IUserRepository
import org.vengeful.citymanager.userService.PasswordHasher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun authenticateUser(
    username: String,
    password: String,
    userRepository: IUserRepository
): User? {
    val user = userRepository.findByUsername(username)

    if (user == null || !user.isActive) {
        return null
    }

    // Проверяем пароль
    return if (PasswordHasher.verifyPassword(password, user.passwordHash)) {
        user
    } else {
        User(
            id = 0,
            "error",
            "",
            listOf(Rights.Any)
        )
    }
}