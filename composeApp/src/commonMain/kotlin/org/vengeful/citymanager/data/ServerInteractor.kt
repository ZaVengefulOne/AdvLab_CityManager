package org.vengeful.citymanager.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

class ServerInteractor() : IServerInteractor {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
        engine {
            https.trustManager = null
        }
    }

    fun setHttpBuilder(): Headers {
        return headers {
            append(HttpHeaders.Accept, HEADER_ACCEPT)
            append(USER_AGENT_TAG, USER_AGENT)
        }
    }


    override suspend fun getPersons(): List<Person> {
        return try {
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons") { setHttpBuilder() }
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

//    suspend fun addPerson(person: Person): Boolean {
//        return try {
//            val response: HttpResponse = client.post("http://localhost:8080/persons") {
//                contentType(ContentType.Application.Json)
//                setBody(person)
//            }
//
//            response.status == HttpStatusCode.OK
//        } catch (e: Exception) {
//            println("Failed to add person: ${e.message}")
//            false
//        }
//    }

    override suspend fun addPerson(person: Person) {
        try {
            val response =
                client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append(USER_AGENT_TAG, USER_AGENT)
                    }
                    setBody(person)
                }
        } catch (e: Exception) {
            throw Exception("Failed to fetch persons: ${e.message}")
        }
    }

    override suspend fun deletePerson(id: Int) {
        try {
            client.delete("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/persons/${id}") { setHttpBuilder() }
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

    companion object {
        const val SERVER_PREFIX = "http://"
        const val SERVER_ADDRESS = "localhost"
        const val USER_AGENT_TAG = "User-agent"
        const val USER_AGENT = "Vengeful-user! Version: 0.0.1"
        const val HEADER_ACCEPT = "application/json"
        const val ERROR_TEXT = "Server Error!"
    }
}