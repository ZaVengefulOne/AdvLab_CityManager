package org.vengeful.citymanager.libraryService

import org.vengeful.citymanager.models.library.Article

interface ILibraryRepository {
    fun getAllArticles(): List<Article>
    fun getArticleById(id: Int): Article?
    fun createArticle(title: String, content: String): Article
    fun deleteArticle(id: Int): Boolean
}
