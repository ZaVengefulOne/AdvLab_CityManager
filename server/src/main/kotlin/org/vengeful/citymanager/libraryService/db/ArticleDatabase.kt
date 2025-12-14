package org.vengeful.citymanager.libraryService.db


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.vengeful.citymanager.models.library.Article

object Articles : IntIdTable("articles") {
    val title = varchar("title", 500)
    val content = text("content")
}

class ArticleDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArticleDao>(Articles)

    var title by Articles.title
    var content by Articles.content

    fun toArticle() = Article(
        id = id.value,
        title = title,
        content = content
    )
}
