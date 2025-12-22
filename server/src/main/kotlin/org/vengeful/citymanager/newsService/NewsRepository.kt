package org.vengeful.citymanager.newsService

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.news.News
import org.vengeful.citymanager.models.news.NewsSource
import org.vengeful.citymanager.newsService.INewsRepository
import org.vengeful.citymanager.newsService.db.NewsDao

class NewsRepository : INewsRepository {
    override fun getAllNews(): List<News> {
        return transaction {
            NewsDao.all().map { it.toNews() }
        }
    }

    override fun getNewsById(id: Int): News? {
        return transaction {
            NewsDao.findById(id)?.toNews()
        }
    }

    override fun createNews(title: String, imageUrl: String, source: NewsSource): News {
        return transaction {
            NewsDao.new {
                this.title = title
                this.imageUrl = imageUrl
                this.sourceType = source.name
            }.toNews()
        }
    }

    override fun deleteNews(id: Int): Boolean {
        return transaction {
            NewsDao.findById(id)?.delete() != null
        }
    }
}
