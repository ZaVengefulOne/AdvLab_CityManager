package org.vengeful.citymanager.models.library

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: Int = 0,
    val title: String,
    val content: String
)
