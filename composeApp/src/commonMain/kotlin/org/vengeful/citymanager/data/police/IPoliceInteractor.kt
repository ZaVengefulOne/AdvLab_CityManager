package org.vengeful.citymanager.data.police

import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.models.Person

interface IPoliceInteractor {
    suspend fun createPoliceRecord(record: PoliceRecord, photoBytes: ByteArray?): PoliceRecord
    suspend fun getPersonsWithRecords(): List<Person>
    suspend fun getPoliceRecordsByPersonId(personId: Int): List<PoliceRecord>
    suspend fun getPoliceRecordById(recordId: Int): PoliceRecord?
    suspend fun updatePoliceRecord(recordId: Int, record: PoliceRecord, photoBytes: ByteArray?): PoliceRecord
    suspend fun deletePoliceRecord(recordId: Int): Boolean
    suspend fun getAllPoliceRecords(): List<PoliceRecord>
    suspend fun getPoliceRecordByFingerprintNumber(fingerprintNumber: Int): PoliceRecord?
}


