package org.vengeful.citymanager.medicService

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.medicService.db.MedicalRecordDao
import org.vengeful.citymanager.medicService.db.MedicalRecords
import org.vengeful.citymanager.models.MedicalRecord
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.Persons

class MedicalRepository {

    fun createMedicalRecord(record: MedicalRecord, healthStatus: String): MedicalRecord = transaction {
        val personDao = PersonDao.findById(record.personId)
            ?: throw IllegalStateException("Person with id ${record.personId} not found")

        personDao.health = healthStatus

        val medicalRecordDao = MedicalRecordDao.new {
            personId = personDao.id
            firstName = record.firstName
            lastName = record.lastName
            gender = record.gender
            dateOfBirth = record.dateOfBirth
            workplace = record.workplace
            doctor = record.doctor
            createdAt = record.createdAt
        }

        medicalRecordDao.toMedicalRecord()
    }

    fun getMedicalRecordsByPersonId(personId: Int): List<MedicalRecord> = transaction {
        MedicalRecordDao.find { MedicalRecords.personId eq personId }
            .map { it.toMedicalRecord() }
    }

    fun updateMedicalRecord(recordId: Int, record: MedicalRecord, healthStatus: String): MedicalRecord = transaction {
        val medicalRecordDao = MedicalRecordDao.findById(recordId)
            ?: throw IllegalStateException("Medical record with id $recordId not found")

        val personDao = PersonDao.findById(record.personId)
            ?: throw IllegalStateException("Person with id ${record.personId} not found")

        personDao.health = healthStatus
        medicalRecordDao.firstName = record.firstName
        medicalRecordDao.lastName = record.lastName
        medicalRecordDao.gender = record.gender
        medicalRecordDao.dateOfBirth = record.dateOfBirth
        medicalRecordDao.workplace = record.workplace
        medicalRecordDao.doctor = record.doctor
        medicalRecordDao.prescribedTreatment = record.prescribedTreatment

        medicalRecordDao.toMedicalRecord()
    }

    fun getMedicalRecordById(recordId: Int): MedicalRecord? = transaction {
        MedicalRecordDao.findById(recordId)?.toMedicalRecord()
    }


    fun getAllMedicalRecords(): List<MedicalRecord> = transaction {
        MedicalRecordDao.all().map { it.toMedicalRecord() }
    }

    fun getPatientsWithRecords(): List<Person> = transaction {
        // Получаем всех персон, у которых есть медицинские карточки
        val personIdsWithRecords = MedicalRecords
            .slice(MedicalRecords.personId)
            .selectAll()
            .distinct()
            .map { it[MedicalRecords.personId].value }

        PersonDao.find { Persons.id inList personIdsWithRecords }
            .map { it.toPerson() }
    }
}
