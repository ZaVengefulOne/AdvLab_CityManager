package org.vengeful.citymanager.newsService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.vengeful.citymanager.models.news.News
import org.vengeful.citymanager.models.news.NewsSource

object NewsTable : IntIdTable("news") {
    val title = varchar("title", 500)
    val imageUrl = varchar("image_url", 1000)
    val sourceType = varchar("source", 50)
}

class NewsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NewsDao>(NewsTable)

    var title by NewsTable.title
    var imageUrl by NewsTable.imageUrl
    var sourceType by NewsTable.sourceType

    fun toNews() = News(
        id = id.value,
        title = title,
        imageUrl = imageUrl,
        source = try {
            NewsSource.valueOf(sourceType)
        } catch (e: IllegalArgumentException) {
            NewsSource.PUBLISHING_HOUSE
        }
    )
}
