package org.vengeful.citymanager.models.news

import kotlinx.serialization.Serializable

@Serializable
data class CreateNewsRequest(
    val title: String? = null,
    val source: NewsSource
)
