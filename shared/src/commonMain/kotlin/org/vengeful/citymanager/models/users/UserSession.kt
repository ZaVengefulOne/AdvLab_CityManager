package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class UserSession @OptIn(ExperimentalTime::class) constructor(
    val sessionId: String,
    val userId: Int,
    val userName: String,
    val rights: List<Rights>,
    val expiresAt: Instant,
    val createdAt: Instant = Clock.System.now(),
)
