package org.vengeful.citymanager.medicService.db


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.vengeful.citymanager.models.MedicalRecord
import org.vengeful.citymanager.personService.db.Persons

object MedicalRecords : IntIdTable("medical_records") {
    val personId = reference("person_id", Persons.id, onDelete = ReferenceOption.CASCADE)
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val gender = varchar("gender", 10)
    val dateOfBirth = long("date_of_birth")
    val workplace = varchar("workplace", 255)
    val doctor = varchar("doctor", 255)
    val prescribedTreatment = varchar("prescribed_treatment", 500).default("")

    val createdAt = long("created_at")
}

class MedicalRecordDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MedicalRecordDao>(MedicalRecords)

    var personId by MedicalRecords.personId
    var firstName by MedicalRecords.firstName
    var lastName by MedicalRecords.lastName
    var gender by MedicalRecords.gender
    var dateOfBirth by MedicalRecords.dateOfBirth
    var workplace by MedicalRecords.workplace
    var doctor by MedicalRecords.doctor
    var prescribedTreatment by MedicalRecords.prescribedTreatment
    var createdAt by MedicalRecords.createdAt

    fun toMedicalRecord() = MedicalRecord(
        id = id.value,
        personId = personId.value,
        firstName = firstName,
        lastName = lastName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        workplace = workplace,
        doctor = doctor,
        prescribedTreatment = prescribedTreatment,
        createdAt = createdAt
    )
}
