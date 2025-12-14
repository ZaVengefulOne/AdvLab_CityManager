package org.vengeful.citymanager.models.library

import kotlinx.serialization.Serializable

@Serializable
data class CreateArticleRequest(
    val title: String,
    val content: String
)
