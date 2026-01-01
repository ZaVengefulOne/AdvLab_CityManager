package org.vengeful.citymanager.data.backup

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.backup.LimitedMasterBackup

class BackupInteractor(
    private val authManager: AuthManager
) : IBackupInteractor {

    private val baseUrl = SERVER_BASE_URL
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun downloadGameBackup(format: String) {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val response = client.get("$baseUrl/backup/game?format=$format") {
            header("Authorization", "Bearer $token")
        }

        val content = response.body<String>()
        val contentType = response.headers[HttpHeaders.ContentType]

        // Вызываем expect функцию на уровне пакета
        saveFile(content, "Бэкап.$format", contentType ?: "text/html")
    }

    override suspend fun downloadLimitedMasterBackup() {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val response = client.get("$baseUrl/backup/master-limited") {
            header("Authorization", "Bearer $token")
        }

        val backup = response.body<LimitedMasterBackup>()
        val jsonString = json.encodeToString(LimitedMasterBackup.serializer(), backup)

        // Сохраняем как JSON файл
        saveFile(jsonString, "master_backup_limited.json", "application/json")
    }

    override suspend fun uploadLimitedMasterBackup(backup: LimitedMasterBackup) {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val jsonString = json.encodeToString(LimitedMasterBackup.serializer(), backup)

        val response = client.post("$baseUrl/backup/restore-limited") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(jsonString)
        }

        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("Ошибка восстановления бэкапа: ${response.status} - $errorText")
        }
    }
}

// expect функция на уровне пакета, а не класса
internal expect fun saveFile(content: String, filename: String, contentType: String)
