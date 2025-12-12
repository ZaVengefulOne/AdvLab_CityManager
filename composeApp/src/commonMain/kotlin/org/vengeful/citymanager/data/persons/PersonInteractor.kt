package org.vengeful.citymanager.data.persons

import io.ktor.client.call.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.http.HttpHeaders
import org.vengeful.citymanager.BUILD_VARIANT
import org.vengeful.citymanager.BuildVariant
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

class PersonInteractor(
    private val authManager: AuthManager
) : IPersonInteractor {

    override suspend fun getPersons(): List<Person> {
        return try {
            val token = authManager.getToken()
            println("DEBUG: Token in getPersons: ${if (token != null) "present (${token.take(20)}...)" else "null"}")
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons") {
                setHttpBuilder(withAuth = token != null) // Добавляем токен только если он есть
            }
            if (response.status.isSuccess()) {
                response.body<List<Person>>()
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun getPersonById(id: Int): Person? {
        return try {
            val response =
                client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/byId/${id}") { setHttpBuilder() }
            if (response.status.isSuccess()) {
                response.body<Person>()
            } else {
                throw Exception("$ERROR_TEXT ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun getPersonByName(
        name: String,
        lastName: String
    ): Person? {
        return try {
            val response =
                client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/byName/${name}_${lastName}") { setHttpBuilder() }
            if (response.status.isSuccess()) {
                response.body<Person>()
            } else {
                throw Exception("$ERROR_TEXT ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun getPersonsByRights(rights: List<Rights>): List<Person> {
        val rightsParam = rights.joinToString(",") { it.name }
        return client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/byRights") {
            setHttpBuilder()
            parameter("rights", rightsParam)
        }.body()
    }

    override suspend fun addPerson(person: Person) {
        try {
            client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons") {
                setHttpBuilder()
                setBody(person)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun updatePerson(person: Person) {
        return try {
            val response = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/update") {
                setHttpBuilder()
                setBody(person)
            }
            if (response.status.isSuccess()) {
                // Удачно, TODO: добавить сообщение?
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to update person: ${e.message}")
        }
    }

    override suspend fun deletePerson(id: Int) {
        try {
            client.delete("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/${id}") {
                setHttpBuilder()
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun getPersonsByRights(rights: Rights): List<Person> {
        return try {
            val response =
                client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/byRights/$rights") { setHttpBuilder() }
            if (response.status.isSuccess()) {
                response.body<List<Person>>()
            } else {
                throw Exception("$ERROR_TEXT ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun getAdminPersons(): List<Person> {
        return try {
            val serverAddress = if (BUILD_VARIANT == BuildVariant.DEBUG) SERVER_ADDRESS_DEBUG else SERVER_ADDRESS
            val url = "$SERVER_PREFIX$serverAddress:$SERVER_PORT/adminPersons"
            val response = client.get(url) {
                contentType(ContentType.Application.Json)
                header(USER_AGENT_TAG, USER_AGENT)
            }
            if (response.status.isSuccess()) {
                response.body<List<Person>>()
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Unable to read error body"
                }
                throw Exception("HTTP error ${response.status} : ${response.status.description}. Body: $errorBody")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to fetch persons: ${e.message}")
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

    override suspend fun transferMoney(fromPersonId: Int, toPersonId: Int, amount: Double) {
        return try {
            val request = org.vengeful.citymanager.models.users.TransferMoneyRequest(
                fromPersonId = fromPersonId,
                toPersonId = toPersonId,
                amount = amount
            )
            val response = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/transfer") {
                setHttpBuilder()
                setBody(request)
            }
            if (!response.status.isSuccess()) {
                val errorBody = try {
                    response.body<Map<String, String>>()["error"] ?: "Unknown error"
                } catch (e: Exception) {
                    "HTTP error ${response.status} : ${response.status.description}"
                }
                throw Exception(errorBody)
            } else {

            }
        } catch (e: Exception) {
            throw Exception("Failed to transfer money: ${e.message}")
        }
    }

    companion object {
        const val SERVER_PREFIX = "http://"
        const val SERVER_ADDRESS = "localhost"
        const val SERVER_ADDRESS_DEBUG = "10.0.2.2"
        const val ERROR_TEXT = "Server Error!"
    }
}
