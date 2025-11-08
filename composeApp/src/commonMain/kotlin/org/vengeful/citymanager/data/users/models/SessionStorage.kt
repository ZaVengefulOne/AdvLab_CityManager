package org.vengeful.citymanager.data.users.models

import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.models.users.UserSession
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object SessionStorage {
    private val activeSessions = mutableMapOf<String, UserSession>()

    fun createSession(user: User): UserSession {
        val creationTime = Clock.System.now()
        val expirationTime = creationTime.plus(3.hours)
        val session = UserSession(
            sessionId = UUID.randomUUID().toString(),
            userId = user.id,
            userName = user.username,
            rights = user.rights,
            expiresAt = expirationTime,
            createdAt = creationTime
        )
        activeSessions[session.sessionId] = session
        return session
    }

    fun getSession(sessionId: String): UserSession? {
        return activeSessions[sessionId]?.takeIf {
            it.expiresAt > Clock.System.now() // TODO: проверить
        }
    }

    fun invalidateSession(sessionId: String) {
        activeSessions.remove(sessionId)
    }

    fun cleanupExpiredSessions() {
        val curTime = Clock.System.now()
        activeSessions.entries.removeAll { it.value.expiresAt < curTime } // TODO: проверить
    }
}