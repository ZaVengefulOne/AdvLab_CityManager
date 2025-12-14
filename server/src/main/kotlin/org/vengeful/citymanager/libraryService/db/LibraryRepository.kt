package org.vengeful.citymanager.libraryService.db


import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.libraryService.ILibraryRepository
import org.vengeful.citymanager.models.library.Article

class LibraryRepository : ILibraryRepository {
    override fun getAllArticles(): List<Article> {
        return transaction {
            ArticleDao.all().map { it.toArticle() }
        }
    }

    override fun getArticleById(id: Int): Article? {
        return transaction {
            ArticleDao.findById(id)?.toArticle()
        }
    }

    override fun createArticle(title: String, content: String): Article {
        return transaction {
            ArticleDao.new {
                this.title = title
                this.content = content
            }.toArticle()
        }
    }

    override fun deleteArticle(id: Int): Boolean {
        return transaction {
            ArticleDao.findById(id)?.delete() != null
        }
    }
}
