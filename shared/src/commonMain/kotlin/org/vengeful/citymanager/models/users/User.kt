package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.Rights
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class User @OptIn(ExperimentalTime::class) constructor(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val rights: List<Rights>,
    val isActive: Boolean = true,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val severiteClicks: Int = 0,
)
