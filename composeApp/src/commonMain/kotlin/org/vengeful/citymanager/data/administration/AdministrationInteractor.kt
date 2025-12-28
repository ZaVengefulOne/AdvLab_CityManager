package org.vengeful.citymanager.data.administration

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.models.CallRequest
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownRequest
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownStatusResponse
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.SendMessageRequest
import org.vengeful.citymanager.models.emergencyShutdown.ErrorResponse

class AdministrationInteractor(private val authManager: AuthManager) : IAdministrationInteractor {

    override suspend fun getAdministrationConfig(): AdministrationConfig {
        return try {
            val token = authManager.getToken()
            val response = client.get("$SERVER_BASE_URL/admin/config") {
                setHttpBuilder(withAuth = token != null)
            }
            if (response.status.isSuccess()) {
                response.body<AdministrationConfig>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch admin config: ${e.message}")
        }
    }

    override suspend fun sendMessage(text: String, sender: String): Boolean {
        return try {
            val response = client.post("$SERVER_BASE_URL/admin/chat/send") {
                setHttpBuilder()
                setBody(SendMessageRequest(text, sender))
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            throw Exception("Failed to send message: ${e.message}")
        }
    }

    override suspend fun callEnterprise(enterprise: Enterprise): Boolean {
        return try {
            val token = authManager.getToken()
            val response = client.post("$SERVER_BASE_URL/call/send") {
                setHttpBuilder(withAuth = token != null)
                setBody(CallRequest(enterprise))
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            throw Exception("Failed to call enterprise: ${e.message}")
        }
    }

    override suspend fun getCallStatus(enterprise: Enterprise): CallStatus {
        return try {
            val token = authManager.getToken()
            val response = client.get("$SERVER_BASE_URL/call/status/${enterprise.name}") {
                setHttpBuilder(withAuth = token != null)
            }
            if (response.status.isSuccess()) {
                response.body<CallStatus>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to get call status: ${e.message}")
        }
    }

    override suspend fun resetCallStatus(enterprise: Enterprise): Boolean {
        return try {
            val token = authManager.getToken()
            val response = client.post("$SERVER_BASE_URL/call/reset/${enterprise.name}") {
                setHttpBuilder(withAuth = token != null)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            throw Exception("Failed to reset call status: ${e.message}")
        }
    }

    override suspend fun activateEmergencyShutdown(durationMinutes: Int, password: String): Boolean {
        return try {
            val token = authManager.getToken()
            val response = client.post("$SERVER_BASE_URL/administration/emergency-shutdown") {
                setHttpBuilder(withAuth = token != null)
                setBody(EmergencyShutdownRequest(durationMinutes, password))
            }

            if (response.status.isSuccess()) {
                true
            } else {
                // Пытаемся получить сообщение об ошибке из ответа
                try {
                    val errorResponse = response.body<ErrorResponse>()
                    throw Exception(errorResponse.error)
                } catch (e: Exception) {
                    // Если не удалось распарсить ErrorResponse, используем общее сообщение
                    if (e.message?.contains("error") == true) {
                        throw e
                    }
                    val errorMessage = when (response.status.value) {
                        403 -> "Хорошая попытка. Введите настоящий пароль."
                        401 -> "Требуется аутентификация."
                        400 -> "Некорректный запрос."
                        else -> "Ошибка при активации экстренного отключения."
                    }
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getEmergencyShutdownStatus(): EmergencyShutdownStatusResponse {
        return try {
            val token = authManager.getToken()
            val response =
                client.get("$SERVER_BASE_URL/administration/emergency-shutdown/status") {
                    setHttpBuilder(withAuth = token != null)
                }
            if (response.status.isSuccess()) {
                response.body<EmergencyShutdownStatusResponse>()
            } else {
                EmergencyShutdownStatusResponse(isActive = false, remainingTimeSeconds = null)
            }
        } catch (e: Exception) {
            EmergencyShutdownStatusResponse(isActive = false, remainingTimeSeconds = null)
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
        contentType(io.ktor.http.ContentType.Application.Json)
        header(USER_AGENT_TAG, USER_AGENT)
        if (withAuth) {
            val token = authManager.getToken()
            if (token != null) {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

}
