package org.vengeful.citymanager.data.backup

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager

class BackupInteractor(
    private val authManager: AuthManager
) : IBackupInteractor {

    private val baseUrl = "http://localhost:8080"

    override suspend fun downloadGameBackup(format: String) {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val response = client.get("$baseUrl/backup/game?format=$format") {
            header("Authorization", "Bearer $token")
        }

        val content = response.body<String>()
        val contentType = response.headers[HttpHeaders.ContentType]

        // Вызываем expect функцию на уровне пакета
        saveFile(content, "game_backup.$format", contentType ?: "text/html")
    }
}

// expect функция на уровне пакета, а не класса
internal expect fun saveFile(content: String, filename: String, contentType: String)