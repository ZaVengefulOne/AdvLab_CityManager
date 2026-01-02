package org.vengeful.citymanager.data.severite

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.severite.AddSeveriteRequest
import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveriteCounts
import org.vengeful.citymanager.models.severite.SeveritePurity
import org.vengeful.citymanager.models.severite.SellSeveriteRequest
import org.vengeful.citymanager.models.severite.SellSeveriteResult

class SeveriteInteractor(
    private val authManager: AuthManager
) : ISeveriteInteractor {

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

    override suspend fun addSeverite(purity: SeveritePurity): Severite {
        return try {
            val request = AddSeveriteRequest(purity = purity.name)
            val response = client.post("$SERVER_BASE_URL/severite/add") {
                setHttpBuilder()
                setBody(request)
            }
            if (response.status.isSuccess()) {
                response.body<Severite>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to add severite: ${e.message}")
        }
    }

    override suspend fun getSeveriteCounts(): SeveriteCounts {
        return try {
            val response = client.get("$SERVER_BASE_URL/severite/counts") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<SeveriteCounts>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch severite counts: ${e.message}")
        }
    }

    override suspend fun getAllSeverite(): List<Severite> {
        return try {
            val response = client.get("$SERVER_BASE_URL/severite/all") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<List<Severite>>()
            } else {
                throw Exception("HTTP error ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch severite: ${e.message}")
        }
    }

    override suspend fun sellSeverite(severiteIds: List<Int>): SellSeveriteResult {
        return try {
            val request = SellSeveriteRequest(severiteIds = severiteIds)
            val response = client.post("$SERVER_BASE_URL/severite/sell") {
                setHttpBuilder()
                setBody(request)
            }
            if (response.status.isSuccess()) {
                response.body<SellSeveriteResult>()
            } else {
                val errorBody = try {
                    response.body<Map<String, String>>()["error"] ?: "Unknown error"
                } catch (e: Exception) {
                    "HTTP error ${response.status}"
                }
                throw Exception(errorBody)
            }
        } catch (e: Exception) {
            throw Exception("Failed to sell severite: ${e.message}")
        }
    }
}




