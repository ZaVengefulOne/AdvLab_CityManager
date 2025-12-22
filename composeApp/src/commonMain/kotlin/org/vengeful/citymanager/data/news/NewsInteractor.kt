package org.vengeful.citymanager.data.news

import io.ktor.client.call.body
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
import org.vengeful.citymanager.models.news.News

class NewsInteractor(
    private val authManager: AuthManager
) : INewsInteractor {

    override suspend fun getAllNews(): List<News> {
        return try {
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/news/items") {
                setHttpBuilder(withAuth = false)
            }
            if (response.status.isSuccess()) {
                response.body<List<News>>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch news: ${e.message}")
        }
    }

    override suspend fun getNewsById(id: Int): News? {
        return try {
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/news/items/$id") {
                setHttpBuilder(withAuth = false)
            }
            if (response.status.isSuccess()) {
                response.body<News>()
            } else if (response.status.value == 404) {
                null
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch news: ${e.message}")
        }
    }


    private fun io.ktor.client.request.HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
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
