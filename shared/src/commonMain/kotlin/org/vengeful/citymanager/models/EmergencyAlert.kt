package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class EmergencyAlert @OptIn(ExperimentalTime::class) constructor(
    val enterprise: Enterprise,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class EmergencyAlertRequest(
    val enterprise: Enterprise
)


