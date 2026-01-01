package org.vengeful.citymanager.data.court

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.court.Hearing

class HearingInteractor(
    private val authManager: AuthManager
) : IHearingInteractor {

    override suspend fun createHearing(hearing: Hearing): Hearing {
        val response = client.post("$SERVER_BASE_URL/court/hearings") {
            setHttpBuilder()
            contentType(ContentType.Application.Json)
            setBody(hearing)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Hearing>()
        } else {
            throw Exception("Failed to create hearing: ${response.status}")
        }
    }

    override suspend fun getAllHearings(): List<Hearing> {
        val response = client.get("$SERVER_BASE_URL/court/hearings") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Hearing>>()
        } else {
            throw Exception("Failed to get hearings: ${response.status}")
        }
    }

    override suspend fun getHearingById(id: Int): Hearing? {
        val response = client.get("$SERVER_BASE_URL/court/hearings/$id") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<Hearing>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get hearing: ${response.status}")
            }
        }
    }

    override suspend fun getHearingByCaseId(caseId: Int): Hearing? {
        val response = client.get("$SERVER_BASE_URL/court/hearings/byCase/$caseId") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<Hearing>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get hearing by case: ${response.status}")
            }
        }
    }

    override suspend fun updateHearing(id: Int, hearing: Hearing): Hearing {
        val response = client.put("$SERVER_BASE_URL/court/hearings/$id") {
            setHttpBuilder()
            contentType(ContentType.Application.Json)
            setBody(hearing)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Hearing>()
        } else {
            throw Exception("Failed to update hearing: ${response.status}")
        }
    }

    override suspend fun deleteHearing(id: Int): Boolean {
        val response = client.delete("$SERVER_BASE_URL/court/hearings/$id") {
            setHttpBuilder()
        }

        return response.status == HttpStatusCode.OK
    }

    private fun io.ktor.client.request.HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
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


