package org.vengeful.citymanager.data.medic

import org.vengeful.citymanager.models.MedicalRecord
import org.vengeful.citymanager.models.Person

interface IMedicInteractor {
    suspend fun createMedicalRecord(record: MedicalRecord, healthStatus: String): MedicalRecord
    suspend fun getPatientsWithRecords(): List<Person>
    suspend fun getMedicalRecordsByPersonId(personId: Int): List<MedicalRecord>
    suspend fun getMedicalRecordById(recordId: Int): MedicalRecord?
    suspend fun updateMedicalRecord(recordId: Int, record: MedicalRecord, healthStatus: String): MedicalRecord

}
