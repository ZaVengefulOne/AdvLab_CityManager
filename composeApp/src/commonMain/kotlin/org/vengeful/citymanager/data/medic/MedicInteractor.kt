package org.vengeful.citymanager.data.medic


import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
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
import org.vengeful.citymanager.models.users.CreateMedicalRecordRequest
import org.vengeful.citymanager.models.medicine.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.medicine.CreateMedicineOrderRequest
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrder
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification
import org.vengeful.citymanager.models.users.UpdateMedicalRecordRequest

class MedicInteractor(
    private val authManager: AuthManager
) : IMedicInteractor {


    override suspend fun createMedicalRecord(record: MedicalRecord, healthStatus: String): MedicalRecord {
        val response = client.post("$SERVER_BASE_URL/medic/medical-records") {
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
        val response = client.get("$SERVER_BASE_URL/medic/patients") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Person>>()
        } else {
            throw Exception("Failed to get patients: ${response.status}")
        }
    }

    override suspend fun getMedicalRecordsByPersonId(personId: Int): List<MedicalRecord> {
        val response = client.get("$SERVER_BASE_URL/medic/medical-records/$personId") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<MedicalRecord>>()
        } else {
            throw Exception("Failed to get medical records: ${response.status}")
        }
    }

    override suspend fun getMedicalRecordById(recordId: Int): MedicalRecord? {
        val response = client.get("$SERVER_BASE_URL/medic/medical-records/byId/$recordId") {
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
        val response = client.put("$SERVER_BASE_URL/medic/medical-records/$recordId") {
            setHttpBuilder()
            setBody(UpdateMedicalRecordRequest(record = record, healthStatus = healthStatus))
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<MedicalRecord>()
        } else {
            throw Exception("Failed to update medical record: ${response.status}")
        }
    }

    override suspend fun getAllMedicines(): List<Medicine> {
        val response = client.get("$SERVER_BASE_URL/medic/medicines") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Medicine>>()
        } else {
            throw Exception("Failed to get medicines: ${response.status}")
        }
    }

    override suspend fun getMedicineById(id: Int): Medicine? {
        val response = client.get("$SERVER_BASE_URL/medic/medicines/$id") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Medicine>()
        } else if (response.status == HttpStatusCode.NotFound) {
            return null
        } else {
            throw Exception("Failed to get medicine: ${response.status}")
        }
    }

    override suspend fun createMedicine(medicine: Medicine): Medicine {
        val response = client.post("$SERVER_BASE_URL/medic/medicines") {
            setHttpBuilder()
            setBody(medicine)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Medicine>()
        } else {
            throw Exception("Failed to create medicine: ${response.status}")
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Medicine {
        val response = client.put("$SERVER_BASE_URL/medic/medicines/${medicine.id}") {
            setHttpBuilder()
            setBody(medicine)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Medicine>()
        } else {
            throw Exception("Failed to update medicine: ${response.status}")
        }
    }

    override suspend fun deleteMedicine(id: Int): Boolean {
        val response = client.delete("$SERVER_BASE_URL/medic/medicines/$id") {
            setHttpBuilder()
        }

        return response.status == HttpStatusCode.OK
    }

    override suspend fun deleteMedicalRecord(recordId: Int): Boolean {
        val response = client.delete("$SERVER_BASE_URL/medic/medical-records/$recordId") {
            setHttpBuilder()
        }

        return response.status == HttpStatusCode.OK
    }

    override suspend fun orderMedicine(medicineId: Int, quantity: Int, accountId: Int): MedicineOrder {
        val response = client.post("$SERVER_BASE_URL/medic/order-medicine") {
            setHttpBuilder()
            setBody(CreateMedicineOrderRequest(medicineId = medicineId, quantity = quantity, accountId = accountId))
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<MedicineOrder>()
        } else {
            val errorBody = try {
                response.body<Map<String, String>>()["error"] ?: "Unknown error"
            } catch (e: Exception) {
                "Failed to order medicine: ${response.status}"
            }
            throw Exception(errorBody)
        }
    }

    override suspend fun getMedicineOrders(): List<MedicineOrderNotification> {
        val response = client.get("$SERVER_BASE_URL/medic/orders") {
            setHttpBuilder()
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<MedicineOrderNotification>>()
        } else {
            throw Exception("Failed to get medicine orders: ${response.status}")
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
