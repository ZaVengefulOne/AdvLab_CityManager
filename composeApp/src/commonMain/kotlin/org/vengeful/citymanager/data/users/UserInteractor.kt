package org.vengeful.citymanager.data.users

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.BUILD_VARIANT
import org.vengeful.citymanager.BuildVariant
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_PREFIX
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS_DEBUG
import org.vengeful.citymanager.data.users.models.LoginResult
import org.vengeful.citymanager.data.users.models.RegisterResult
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.emergencyShutdown.ErrorResponse
import org.vengeful.citymanager.models.users.AuthResponse
import org.vengeful.citymanager.models.users.LoginRequest
import org.vengeful.citymanager.models.users.RegisterRequest
import org.vengeful.citymanager.models.users.UpdateClicksRequest
import org.vengeful.citymanager.models.users.UpdateUserRequest
import org.vengeful.citymanager.models.users.User

class UserInteractor(private val authManager: AuthManager) : IUserInteractor {

    override suspend fun login(username: String, password: String): LoginResult {
        return try {
            val loginRequest = LoginRequest(username, password)
            val response: HttpResponse = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
                setAuthHeader()
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                authManager.saveToken(authResponse.token)
                print(authResponse.token)
                authManager.saveUserInfo(
                    username = authResponse.user.username,
                    rights = authResponse.user.rights,
                    clicks = authResponse.user.severiteClicks,
                    userId = authResponse.user.id,
                )
                LoginResult.Success
            } else {
                // Пытаемся получить сообщение об ошибке из ответа
                val errorMessage = try {
                    // Сначала пробуем ErrorResponse
                    try {
                        val errorResponse = response.body<ErrorResponse>()
                        errorResponse.error
                    } catch (e: Exception) {
                        // Если не получилось, пробуем Map
                        try {
                            val errorBody = response.body<Map<String, String>>()
                            errorBody["error"] ?: getDefaultErrorMessage(response.status.value)
                        } catch (e2: Exception) {
                            getDefaultErrorMessage(response.status.value)
                        }
                    }
                } catch (e: Exception) {
                    getDefaultErrorMessage(response.status.value)
                }
                LoginResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                e.message?.contains("serialization", ignoreCase = true) == true ->
                    "Ошибка обработки ответа сервера"

                else -> "Ошибка: ${e.message ?: "Не удалось выполнить вход. Попробуйте позже."}"
            }
            LoginResult.Error(errorMessage)
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "Указанного профиля не существует"
            400 -> "Некорректный запрос"
            403 -> "Неверный пароль"
            503 -> "Система под блокировкой. Повторите попытку позже"
            else -> "Ошибка сервера: $statusCode"
        }
    }

    override suspend fun logout(): Boolean {
        authManager.clearToken()
        return authManager.getToken() == null
    }

    override suspend fun updateClicks(userId: Int, clicks: Int): Boolean {
        return try {
            val token = authManager.getToken()
            if (token == null) {
                println("updateClicks: ERROR - No token in AuthManager")
                return false
            }
            println("updateClicks: userId=$userId, clicks=$clicks, token present=${token.isNotEmpty()}")

            val response: HttpResponse = client.put("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/users/$userId/clicks") {
                contentType(ContentType.Application.Json)
                setAuthHeader()
                setBody(UpdateClicksRequest(clicks))
            }
            println("updateClicks: response status=${response.status.value}")
            if (!response.status.isSuccess()) {
                val errorBody = response.body<String>()
                println("updateClicks: error response body=$errorBody")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("Error updating clicks: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun getCurrentUserClicks(): Int? {
        return try {
            val userId = authManager.getUserId() ?: return null
            val users = getAllUsers()
            users.find { it.id == userId }?.severiteClicks
        } catch (e: Exception) {
            println("Error getting user clicks: ${e.message}")
            null
        }
    }

    override suspend fun adminRegister(
        username: String,
        password: String,
        personId: Int?,
        rights: List<Rights>
    ): RegisterResult {
        return try {
            val serverAddress = if (BUILD_VARIANT == BuildVariant.DEBUG) SERVER_ADDRESS_DEBUG else SERVER_ADDRESS
            val registerRequest = RegisterRequest(username, password, personId, rights)
            val response: HttpResponse = client.post("$SERVER_PREFIX$serverAddress:$SERVER_PORT/adminReg") {
                contentType(ContentType.Application.Json)
                setBody(registerRequest)
                // Не добавляем токен авторизации
            }

            if (response.status.isSuccess()) {
                RegisterResult.Success
            } else {
                val errorMessage = when (response.status.value) {
                    400 -> {
                        try {
                            val errorBody = response.body<Map<String, String>>()
                            errorBody["error"] ?: "Некорректный запрос"
                        } catch (e: Exception) {
                            "Некорректный запрос"
                        }
                    }
                    409 -> "Пользователь с таким именем уже существует!"
                    else -> "Ошибка сервера: ${response.status.description}"
                }
                RegisterResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                else -> "Ошибка: ${e.message ?: "Не удалось зарегистрироваться. Попробуйте позже."}"
            }
            RegisterResult.Error(errorMessage)
        }
    }

    override suspend fun register(username: String, password: String, personId: Int?, rights: List<Rights>): RegisterResult {
        return try {
            val registerRequest = RegisterRequest(username, password, personId, rights)
            val response: HttpResponse = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(registerRequest)
            }

            if (response.status.isSuccess()) {
                RegisterResult.Success
            } else {
                val errorMessage = when (response.status.value) {
                    400 -> {
                        val errorBody = response.body<Map<String, String>>()
                        errorBody["error"] ?: "Некорректный запрос"
                    }
                    409 -> "Пользователь с таким именем уже существует!"
                    else -> "Ошибка сервера: ${response.status.description}"
                }
                RegisterResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                else -> "Ошибка: ${e.message ?: "Не удалось зарегистрироваться. Попробуйте позже."}"
            }
            RegisterResult.Error(errorMessage)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val response: HttpResponse = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/users") {
                contentType(ContentType.Application.Json)
                setHttpBuilder()
            }

            if (response.status.isSuccess()) {
                response.body<List<User>>()
            } else {
                val errorMessage = when (response.status.value) {
                    401 -> "Требуется аутентификация"
                    403 -> "Недостаточно прав доступа"
                    404 -> "Ресурс не найден"
                    else -> "Ошибка сервера: ${response.status.description}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                e.message?.contains("serialization", ignoreCase = true) == true ->
                    "Ошибка обработки ответа сервера"

                e.message?.contains("401") == true ->
                    "Требуется аутентификация. Пожалуйста, войдите в систему."

                e.message?.contains("403") == true ->
                    "Недостаточно прав доступа для выполнения операции"

                else -> "Ошибка: ${e.message ?: "Не удалось получить список пользователей. Попробуйте позже."}"
            }
            throw Exception(errorMessage)
        }
    }

    /**
     * Обновить пользователя
     */
    override suspend fun updateUser(user: User, password: String?, personId: Int?): Boolean {
        return try {
            // Создаем UpdateUserRequest из User и дополнительных параметров
            val updateRequest = UpdateUserRequest(
                id = user.id,
                username = user.username,
                password = password, // Опциональный пароль
                rights = user.rights,
                isActive = user.isActive,
                personId = personId // ID связанного Person
            )

            val response: HttpResponse = client.put("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/users/${user.id}") {
                contentType(ContentType.Application.Json)
                setBody(updateRequest)
                setHttpBuilder()
            }

            if (response.status.isSuccess()) {
                true
            } else {
                val errorMessage = when (response.status.value) {
                    400 -> {
                        try {
                            val errorBody = response.body<Map<String, String>>()
                            errorBody["error"] ?: "Некорректный запрос"
                        } catch (e: Exception) {
                            "Некорректный запрос"
                        }
                    }
                    401 -> "Требуется аутентификация"
                    403 -> "Недостаточно прав доступа"
                    404 -> "Пользователь не найден"
                    409 -> "Пользователь с таким именем уже существует"
                    else -> "Ошибка сервера: ${response.status.description}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                e.message?.contains("serialization", ignoreCase = true) == true ->
                    "Ошибка обработки данных. Проверьте корректность введенных данных."

                e.message?.contains("401") == true ->
                    "Требуется аутентификация. Пожалуйста, войдите в систему."

                e.message?.contains("403") == true ->
                    "Недостаточно прав доступа для обновления пользователя"

                e.message?.contains("404") == true ->
                    "Пользователь не найден"

                e.message?.contains("409") == true ->
                    "Пользователь с таким именем уже существует"

                else -> "Ошибка: ${e.message ?: "Не удалось обновить пользователя. Попробуйте позже."}"
            }
            throw Exception(errorMessage)
        }
    }

    /**
     * Удалить пользователя
     */
    override suspend fun deleteUser(id: Int): Boolean {
        return try {
            val response: HttpResponse = client.delete("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/users/$id") {
                contentType(ContentType.Application.Json)
                setHttpBuilder()
            }

            if (response.status.isSuccess()) {
                true
            } else {
                val errorMessage = when (response.status.value) {
                    400 -> {
                        try {
                            val errorBody = response.body<Map<String, String>>()
                            errorBody["error"] ?: "Некорректный запрос"
                        } catch (e: Exception) {
                            "Некорректный запрос"
                        }
                    }
                    401 -> "Требуется аутентификация"
                    403 -> "Недостаточно прав доступа"
                    404 -> "Пользователь не найден"
                    else -> "Ошибка сервера: ${response.status.description}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Превышено время ожидания. Проверьте подключение к серверу."

                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Не удалось подключиться к серверу. Проверьте подключение к сети."

                e.message?.contains("serialization", ignoreCase = true) == true ->
                    "Ошибка обработки ответа сервера"

                e.message?.contains("401") == true ->
                    "Требуется аутентификация. Пожалуйста, войдите в систему."

                e.message?.contains("403") == true ->
                    "Недостаточно прав доступа для удаления пользователя"

                e.message?.contains("404") == true ->
                    "Пользователь не найден"

                else -> "Ошибка: ${e.message ?: "Не удалось удалить пользователя. Попробуйте позже."}"
            }
            throw Exception(errorMessage)
        }
    }

    // Модифицируем существующие методы для добавления JWT токена
    fun HttpRequestBuilder.setAuthHeader() {
        val token = authManager.getToken()
        println("setAuthHeader: token=${if (token != null) "present (${token.take(20)}...)" else "null"}")
        if (token != null) {
            header(HttpHeaders.Authorization, "Bearer $token")  // Использовать header вместо headers { append }
        } else {
            println("WARNING: No token found in AuthManager for authenticated request")
        }
    }

    fun HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
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
