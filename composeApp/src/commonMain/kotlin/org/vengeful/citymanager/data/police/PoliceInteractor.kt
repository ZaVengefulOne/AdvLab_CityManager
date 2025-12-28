package org.vengeful.citymanager.data.police

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.models.Person

class PoliceInteractor(
    private val authManager: AuthManager
) : IPoliceInteractor {

    override suspend fun createPoliceRecord(record: PoliceRecord, photoBytes: ByteArray?): PoliceRecord {
        val token = authManager.getToken()
        
        val recordJson = Json.encodeToString(PoliceRecord.serializer(), record)
        
        val response = if (photoBytes != null) {
            // Multipart запрос с фото
            client.post("$SERVER_BASE_URL/police/records") {
                setHttpBuilder()
                setBody(MultiPartFormDataContent(
                    formData {
                        append("record", recordJson)
                        append("photo", photoBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=photo.png")
                        })
                    }
                ))
            }
        } else {
            // Обычный JSON запрос без фото - но сервер ожидает multipart, поэтому отправляем без фото
            client.post("$SERVER_BASE_URL/police/records") {
                setHttpBuilder()
                setBody(MultiPartFormDataContent(
                    formData {
                        append("record", recordJson)
                    }
                ))
            }
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<PoliceRecord>()
        } else {
            throw Exception("Failed to create police record: ${response.status}")
        }
    }

    override suspend fun getPersonsWithRecords(): List<Person> {
        val response = client.get("$SERVER_BASE_URL/police/persons") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Person>>()
        } else {
            throw Exception("Failed to get persons: ${response.status}")
        }
    }

    override suspend fun getPoliceRecordsByPersonId(personId: Int): List<PoliceRecord> {
        val response = client.get("$SERVER_BASE_URL/police/records/$personId") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<PoliceRecord>>()
        } else {
            throw Exception("Failed to get police records: ${response.status}")
        }
    }

    override suspend fun getPoliceRecordById(recordId: Int): PoliceRecord? {
        val response = client.get("$SERVER_BASE_URL/police/records/byId/$recordId") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<PoliceRecord>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get police record: ${response.status}")
            }
        }
    }

    override suspend fun updatePoliceRecord(recordId: Int, record: PoliceRecord, photoBytes: ByteArray?): PoliceRecord {
        val recordJson = Json.encodeToString(PoliceRecord.serializer(), record)
        
        val response = if (photoBytes != null) {
            // Multipart запрос с фото
            client.put("$SERVER_BASE_URL/police/records/$recordId") {
                setHttpBuilder()
                setBody(MultiPartFormDataContent(
                    formData {
                        append("record", recordJson)
                        append("photo", photoBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=photo.png")
                        })
                    }
                ))
            }
        } else {
            // Multipart запрос без фото
            client.put("$SERVER_BASE_URL/police/records/$recordId") {
                setHttpBuilder()
                setBody(MultiPartFormDataContent(
                    formData {
                        append("record", recordJson)
                    }
                ))
            }
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<PoliceRecord>()
        } else {
            throw Exception("Failed to update police record: ${response.status}")
        }
    }

    override suspend fun deletePoliceRecord(recordId: Int): Boolean {
        val response = client.delete("$SERVER_BASE_URL/police/records/$recordId") {
            setHttpBuilder()
        }

        return response.status == HttpStatusCode.OK
    }

    override suspend fun getAllPoliceRecords(): List<PoliceRecord> {
        val response = client.get("$SERVER_BASE_URL/police/records") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<PoliceRecord>>()
        } else {
            throw Exception("Failed to get police records: ${response.status}")
        }
    }

    override suspend fun getPoliceRecordByFingerprintNumber(fingerprintNumber: Int): PoliceRecord? {
        val response = client.get("$SERVER_BASE_URL/police/records/byFingerprint/$fingerprintNumber") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<PoliceRecord>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get police record: ${response.status}")
            }
        }
    }

    private fun HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
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

