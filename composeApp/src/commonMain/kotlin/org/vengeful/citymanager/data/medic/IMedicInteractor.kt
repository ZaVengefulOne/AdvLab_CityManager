package org.vengeful.citymanager.data.medic

import org.vengeful.citymanager.models.medicine.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrder
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification

interface IMedicInteractor {
    suspend fun createMedicalRecord(record: MedicalRecord, healthStatus: String): MedicalRecord
    suspend fun getPatientsWithRecords(): List<Person>
    suspend fun getMedicalRecordsByPersonId(personId: Int): List<MedicalRecord>
    suspend fun getMedicalRecordById(recordId: Int): MedicalRecord?
    suspend fun updateMedicalRecord(recordId: Int, record: MedicalRecord, healthStatus: String): MedicalRecord

    suspend fun getAllMedicines(): List<Medicine>
    suspend fun getMedicineById(id: Int): Medicine?
    suspend fun createMedicine(medicine: Medicine): Medicine
    suspend fun updateMedicine(medicine: Medicine): Medicine
    suspend fun deleteMedicine(id: Int): Boolean
    suspend fun deleteMedicalRecord(recordId: Int): Boolean

    suspend fun orderMedicine(medicineId: Int, quantity: Int, accountId: Int): MedicineOrder
    suspend fun getMedicineOrders(): List<MedicineOrderNotification>
}
