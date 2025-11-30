package org.vengeful.cityManager

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vengeful.cityManager.models.RequestLog
import org.vengeful.cityManager.models.ServerStats
import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.models.SendMessageRequest
import org.vengeful.citymanager.models.backup.MasterBackup
import org.vengeful.citymanager.models.users.AuthResponse
import org.vengeful.citymanager.models.users.LoginRequest

// Модели данных




class ApiClient(
    private val authManager: AuthManager,
    private val onUnauthorized: () -> Unit = {}
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    // Базовый URL твоего Ktor сервера
    private val baseUrl = "http://localhost:8080"


    private fun HttpRequestBuilder.addAuthHeader() {
        val token = authManager.getToken()
        if (token != null) {
            header("Authorization", "Bearer $token")
        }
    }

    private suspend fun <T> handleResponse(response: io.ktor.client.statement.HttpResponse, block: suspend () -> T): T {
        if (response.status == HttpStatusCode.Unauthorized) {
            // Токен истек или невалиден
            authManager.clearToken()
            onUnauthorized()
            throw Exception("Сессия истекла. Пожалуйста, войдите снова.")
        }
        if (!response.status.value.toString().startsWith("2")) {
            val errorText = response.body<String>()
            throw Exception("Server error: ${response.status} - $errorText")
        }
        return block()
    }

    suspend fun login(username: String, password: String): AuthResponse {
        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }

        return handleResponse(response) {
            response.body<AuthResponse>().also {
                authManager.saveToken(it.token)
            }
        }
    }

    suspend fun getServerStats(): ServerStats {
        val response = client.get("$baseUrl/admin/stats") {
            addAuthHeader()
        }
        return handleResponse(response) {
            response.body<ServerStats>()
        }
    }

    suspend fun getRequestLogs(): List<RequestLog> {
        val response = client.get("$baseUrl/admin/logs") {
            addAuthHeader()
        }
        return handleResponse(response) {
            response.body<List<RequestLog>>()
        }
    }

    suspend fun clearLogs() {
        val response = client.post("$baseUrl/admin/clear-logs") {
            addAuthHeader()
        }
        handleResponse(response) { }
    }

    suspend fun getMasterBackup(): MasterBackup {
        val response = client.get("$baseUrl/backup/master") {
            addAuthHeader()
        }
        return handleResponse(response) {
            response.body<MasterBackup>()
        }
    }

    suspend fun restoreMasterBackup(backup: MasterBackup) {
        val response = client.post("$baseUrl/backup/restore") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
            setBody(backup)
        }
        handleResponse(response) { }
    }

    suspend fun getConfig(): AdministrationConfig {
        val response = client.get("$baseUrl/admin/config") {
            addAuthHeader()
        }
        return handleResponse(response) {
            response.body<AdministrationConfig>()
        }
    }

    suspend fun updateConfig(config: AdministrationConfig) {
        val response = client.post("$baseUrl/admin/config") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
            setBody(config)
        }
        handleResponse(response) { }
    }

    suspend fun sendChatMessage(text: String): Boolean {
        val response = client.post("$baseUrl/admin/chat/send") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
            setBody(SendMessageRequest(text, "admin"))
        }
        return handleResponse(response) {
            response.status.value in 200..299
        }
    }
}
