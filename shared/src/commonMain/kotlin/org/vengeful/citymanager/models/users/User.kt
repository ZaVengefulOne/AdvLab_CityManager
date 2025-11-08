package org.vengeful.citymanager.models.users

import org.vengeful.citymanager.models.Rights
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class User @OptIn(ExperimentalTime::class) constructor(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val rights: List<Rights>,
    val isActive: Boolean = true,
    val createdAt: Instant = Clock.System.now(),
)
