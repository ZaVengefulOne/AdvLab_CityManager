package org.vengeful.citymanager.data.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vengeful.citymanager.SERVER_ADDRESS_DEBUG
import org.vengeful.citymanager.SERVER_ANDROID_URL
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import java.io.File
import java.io.FileOutputStream

class AndroidBackupInteractor(
    private val authManager: AuthManager,
    private val context: Context
) : IBackupInteractor {

    private val baseUrl = SERVER_ANDROID_URL

    override suspend fun downloadGameBackup(format: String) {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val response = client.get("$baseUrl/backup/game?format=$format") {
            header("Authorization", "Bearer $token")
        }

        val content = response.body<String>()
        val contentType = response.headers[HttpHeaders.ContentType]

        saveFileWithContext(context, content, "Бэкап.$format", contentType ?: "text/html")
    }

    override suspend fun downloadLimitedMasterBackup() {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val response = client.get("$baseUrl/backup/master-limited") {
            header("Authorization", "Bearer $token")
        }

        val backup = response.body<org.vengeful.citymanager.models.backup.LimitedMasterBackup>()
        val jsonString = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            encodeDefaults = true
        }.encodeToString(org.vengeful.citymanager.models.backup.LimitedMasterBackup.serializer(), backup)

        saveFileWithContext(context, jsonString, "master_backup_limited.json", "application/json")
    }

    override suspend fun uploadLimitedMasterBackup(backup: org.vengeful.citymanager.models.backup.LimitedMasterBackup) {
        val token = authManager.getToken() ?: throw Exception("Не авторизован")

        val jsonString = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            encodeDefaults = true
        }.encodeToString(org.vengeful.citymanager.models.backup.LimitedMasterBackup.serializer(), backup)

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

    private suspend fun saveFileWithContext(context: Context, content: String, filename: String, contentType: String) {
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ - используем MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, contentType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        ?: throw Exception("Failed to create file")

                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    } ?: throw Exception("Failed to open output stream")
                } else {
                    // Android 9 и ниже - используем прямой доступ к файлам
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs()
                    }

                    val file = File(downloadsDir, filename)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            } catch (e: Exception) {
                throw Exception("Failed to save file: ${e.message}", e)
            }
        }
    }
}

