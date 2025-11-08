package org.vengeful.citymanager.data.users

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.PersonInteractor.Companion.SERVER_ADDRESS
import org.vengeful.citymanager.data.PersonInteractor.Companion.SERVER_PREFIX
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.models.users.AuthResponse
import org.vengeful.citymanager.models.users.LoginRequest

class UserInteractor(private val authManager: AuthManager) : IUserInteractor {

   override suspend fun login(username: String, password: String): Boolean {
        return try {
            val loginRequest = LoginRequest(username, password)
            val response: HttpResponse = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                authManager.saveToken(authResponse.token)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun logout(): Boolean {
        authManager.clearToken()
        return authManager.getToken() == null
    }

    // Модифицируем существующие методы для добавления JWT токена
    fun HttpRequestBuilder.setAuthHeader() {
        val token = authManager.getToken()
        if (token != null) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    fun HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
        contentType(ContentType.Application.Json)
        headers {
            append(USER_AGENT_TAG, USER_AGENT)
            if (withAuth) {
                val token = authManager.getToken()
                if (token != null) {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }
}