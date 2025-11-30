package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class CallStatus(
    val enterprise: Enterprise,
    val isCalled: Boolean,
    val calledAt: Long? = null
)
