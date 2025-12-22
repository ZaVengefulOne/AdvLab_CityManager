package org.vengeful.citymanager.models.news

import kotlinx.serialization.Serializable


@Serializable
data class News(
    val id: Int = 0,
    val title: String,
    val imageUrl: String,
    val source: NewsSource
)

@Serializable
enum class NewsSource {
    PUBLISHING_HOUSE,
    EBONY_BAY
}
