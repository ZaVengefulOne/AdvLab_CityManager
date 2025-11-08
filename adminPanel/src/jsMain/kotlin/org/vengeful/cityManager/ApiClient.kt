package org.vengeful.cityManager

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vengeful.cityManager.models.RequestLog
import org.vengeful.cityManager.models.ServerStats

// Модели данных




class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Базовый URL твоего Ktor сервера
    private val baseUrl = "http://localhost:8080"

    suspend fun getServerStats(): ServerStats {
        return client.get("$baseUrl/admin/stats").body()
    }

    suspend fun getRequestLogs(): List<RequestLog> {
        return client.get("$baseUrl/admin/logs").body()
    }

    suspend fun clearLogs() {
        client.post("$baseUrl/admin/clear-logs")
    }

    suspend fun exportData(): String {
        return client.get("$baseUrl/admin/export").body()
    }
}