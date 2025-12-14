package org.vengeful.citymanager.data.library

import org.vengeful.citymanager.models.library.Article

interface ILibraryInteractor {
    suspend fun getAllArticles(): List<Article>
    suspend fun getArticleById(id: Int): Article?
}
