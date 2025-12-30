package org.vengeful.citymanager.data.police

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
import kotlinx.serialization.json.Json
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus

class CaseInteractor(
    private val authManager: AuthManager
) : ICaseInteractor {

    override suspend fun createCase(case: Case): Case {
        val response = client.post("$SERVER_BASE_URL/police/cases") {
            setHttpBuilder()
            contentType(ContentType.Application.Json)
            setBody(case)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Case>()
        } else {
            throw Exception("Failed to create case: ${response.status}")
        }
    }

    override suspend fun getAllCases(): List<Case> {
        val response = client.get("$SERVER_BASE_URL/police/cases") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Case>>()
        } else {
            throw Exception("Failed to get cases: ${response.status}")
        }
    }

    override suspend fun getCaseById(caseId: Int): Case? {
        val response = client.get("$SERVER_BASE_URL/police/cases/$caseId") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<Case>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get case: ${response.status}")
            }
        }
    }

    override suspend fun getCasesBySuspect(personId: Int): List<Case> {
        val response = client.get("$SERVER_BASE_URL/police/cases/bySuspect/$personId") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Case>>()
        } else {
            throw Exception("Failed to get cases by suspect: ${response.status}")
        }
    }

    override suspend fun getCasesByInvestigator(personId: Int): List<Case> {
        val response = client.get("$SERVER_BASE_URL/police/cases/byInvestigator/$personId") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Case>>()
        } else {
            throw Exception("Failed to get cases by investigator: ${response.status}")
        }
    }

    override suspend fun updateCase(caseId: Int, case: Case): Case {
        val response = client.put("$SERVER_BASE_URL/police/cases/$caseId") {
            setHttpBuilder()
            contentType(ContentType.Application.Json)
            setBody(case)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Case>()
        } else {
            throw Exception("Failed to update case: ${response.status}")
        }
    }

    override suspend fun updateCaseStatus(caseId: Int, status: CaseStatus): Case {
        val response = client.put("$SERVER_BASE_URL/police/cases/$caseId/status") {
            setHttpBuilder()
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status.name))
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Case>()
        } else {
            throw Exception("Failed to update case status: ${response.status}")
        }
    }

    override suspend fun deleteCase(caseId: Int): Boolean {
        val response = client.delete("$SERVER_BASE_URL/police/cases/$caseId") {
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

