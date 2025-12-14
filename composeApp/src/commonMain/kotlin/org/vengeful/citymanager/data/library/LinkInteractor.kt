package org.vengeful.citymanager.data.library


import com.sun.security.ntlm.Client
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_PREFIX
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.library.Article

class LibraryInteractor(
    private val authManager: AuthManager
) : ILibraryInteractor {

    override suspend fun getAllArticles(): List<Article> {
        return try {
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/library/articles") {
                setHttpBuilder(withAuth = false)
            }
            if (response.status.isSuccess()) {
                response.body<List<Article>>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch articles: ${e.message}")
        }
    }

    override suspend fun getArticleById(id: Int): Article? {
        return try {
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/library/articles/$id") {
                setHttpBuilder(withAuth = false)
            }
            if (response.status.isSuccess()) {
                response.body<Article>()
            } else if (response.status.value == 404) {
                null
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch article: ${e.message}")
        }
    }

    private fun HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
        contentType(ContentType.Application.Json)
        header(USER_AGENT_TAG, USER_AGENT)
        if (withAuth) {
            val token = authManager.getToken()
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            } else {
                println("WARNING: No token found in AuthManager for authenticated request")
            }
        }
    }
}
