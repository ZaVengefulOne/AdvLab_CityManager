package org.vengeful.citymanager.data.administration

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_PREFIX
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.AdministrationConfig

class AdministrationInteractor(private val authManager: AuthManager) : IAdministrationInteractor {

    override suspend fun getAdministrationConfig(): AdministrationConfig {
        return try {
            val token = authManager.getToken()
            val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/admin/config") {
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
