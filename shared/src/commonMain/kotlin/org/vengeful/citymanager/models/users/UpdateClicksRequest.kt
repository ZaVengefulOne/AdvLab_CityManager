package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateClicksRequest(
    val clicks: Int
)