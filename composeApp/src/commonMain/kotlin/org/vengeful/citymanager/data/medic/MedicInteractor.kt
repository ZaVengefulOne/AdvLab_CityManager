package org.vengeful.citymanager.data.medic


import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.vengeful.citymanager.BUILD_VARIANT
import org.vengeful.citymanager.BuildVariant
import org.vengeful.citymanager.SERVER_PORT
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_ADDRESS_DEBUG
import org.vengeful.citymanager.data.persons.PersonInteractor.Companion.SERVER_PREFIX
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.CreateMedicalRecordRequest
import org.vengeful.citymanager.models.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.UpdateMedicalRecordRequest

class MedicInteractor(
    private val authManager: AuthManager
) : IMedicInteractor {


    override suspend fun createMedicalRecord(record: MedicalRecord, healthStatus: String): MedicalRecord {
        val response = client.post("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/medic/medical-records") {
            setHttpBuilder()
            setBody(CreateMedicalRecordRequest(record = record, healthStatus = healthStatus))
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<MedicalRecord>()
        } else {
            throw Exception("Failed to create medical record: ${response.status}")
        }
    }

    override suspend fun getPatientsWithRecords(): List<Person> {
        val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/medic/patients") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Person>>()
        } else {
            throw Exception("Failed to get patients: ${response.status}")
        }
    }

    override suspend fun getMedicalRecordsByPersonId(personId: Int): List<MedicalRecord> {
        val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/medic/medical-records/$personId") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<MedicalRecord>>()
        } else {
            throw Exception("Failed to get medical records: ${response.status}")
        }
    }

    override suspend fun getMedicalRecordById(recordId: Int): MedicalRecord? {
        val response = client.get("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/medic/medical-records/byId/$recordId") {
            setHttpBuilder()
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.body<MedicalRecord>()
            }
            HttpStatusCode.NotFound -> {
                null
            }
            else -> {
                throw Exception("Failed to get medical record: ${response.status}")
            }
        }
    }

    override suspend fun updateMedicalRecord(recordId: Int, record: MedicalRecord, healthStatus: String): MedicalRecord {
        val response = client.put("$SERVER_PREFIX$SERVER_ADDRESS:$SERVER_PORT/medic/medical-records/$recordId") {
            setHttpBuilder()
            setBody(UpdateMedicalRecordRequest(record = record, healthStatus = healthStatus))
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<MedicalRecord>()
        } else {
            throw Exception("Failed to update medical record: ${response.status}")
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
}
