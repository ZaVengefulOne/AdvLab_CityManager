package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class CallRequest(
    val enterprise: Enterprise
)
