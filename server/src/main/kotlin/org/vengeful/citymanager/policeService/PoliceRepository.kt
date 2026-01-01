package org.vengeful.citymanager.policeService

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.policeService.db.PoliceRecordDao
import org.vengeful.citymanager.policeService.db.PoliceRecords

class PoliceRepository {

    fun createPoliceRecord(record: PoliceRecord, newPhotoUrl: String?): PoliceRecord = transaction {
        val personDao = PersonDao.findById(record.personId)
            ?: throw IllegalStateException("Person with id ${record.personId} not found")

        val policeRecordDao = PoliceRecordDao.new {
            personId = personDao.id
            firstName = record.firstName
            lastName = record.lastName
            dateOfBirth = record.dateOfBirth
            workplace = record.workplace
            photoUrl = newPhotoUrl
            fingerprintNumber = record.fingerprintNumber
            createdAt = record.createdAt
        }

        policeRecordDao.toPoliceRecord()
    }

    fun getPoliceRecordsByPersonId(personId: Int): List<PoliceRecord> = transaction {
        PoliceRecordDao.find { PoliceRecords.personId eq personId }
            .map { it.toPoliceRecord() }
    }

    fun updatePoliceRecord(recordId: Int, record: PoliceRecord, photoUrl: String?): PoliceRecord = transaction {
        val policeRecordDao = PoliceRecordDao.findById(recordId)
            ?: throw IllegalStateException("Police record with id $recordId not found")

        val personDao = PersonDao.findById(record.personId)
            ?: throw IllegalStateException("Person with id ${record.personId} not found")

        policeRecordDao.firstName = record.firstName
        policeRecordDao.lastName = record.lastName
        policeRecordDao.dateOfBirth = record.dateOfBirth
        policeRecordDao.workplace = record.workplace
        if (photoUrl != null) {
            policeRecordDao.photoUrl = photoUrl
        }
        policeRecordDao.fingerprintNumber = record.fingerprintNumber

        policeRecordDao.toPoliceRecord()
    }

    fun deletePoliceRecord(recordId: Int): Boolean = transaction {
        val policeRecordDao = PoliceRecordDao.findById(recordId)
        if (policeRecordDao != null) {
            policeRecordDao.delete()
            true
        } else {
            false
        }
    }

    fun getPoliceRecordById(recordId: Int): PoliceRecord? = transaction {
        PoliceRecordDao.findById(recordId)?.toPoliceRecord()
    }

    fun getAllPoliceRecords(): List<PoliceRecord> = transaction {
        PoliceRecordDao.all().map { it.toPoliceRecord() }
    }

    fun getPersonsWithRecords(): List<Person> = transaction {
        val personIdsWithRecords = PoliceRecords
            .slice(PoliceRecords.personId)
            .selectAll()
            .distinct()
            .map { it[PoliceRecords.personId].value }

        PersonDao.find { Persons.id inList personIdsWithRecords }
            .map { it.toPerson() }
    }

    fun getPoliceRecordByFingerprintNumber(fingerprintNumber: Int): PoliceRecord? = transaction {
        PoliceRecordDao.find { PoliceRecords.fingerprintNumber eq fingerprintNumber }
            .firstOrNull()
            ?.toPoliceRecord()
    }
}



