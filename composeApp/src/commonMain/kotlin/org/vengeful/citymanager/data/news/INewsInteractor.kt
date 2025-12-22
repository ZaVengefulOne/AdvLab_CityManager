package org.vengeful.citymanager.data.news

import org.vengeful.citymanager.models.news.News

interface INewsInteractor {
    suspend fun getAllNews(): List<News>
    suspend fun getNewsById(id: Int): News?
}
