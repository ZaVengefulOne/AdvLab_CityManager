package org.vengeful.citymanager.newsService

import org.vengeful.citymanager.models.news.News
import org.vengeful.citymanager.models.news.NewsSource

interface INewsRepository {
    fun getAllNews(): List<News>
    fun getNewsById(id: Int): News?
    fun createNews(title: String, imageUrl: String, source: NewsSource): News
    fun deleteNews(id: Int): Boolean
}
