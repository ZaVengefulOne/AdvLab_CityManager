package org.vengeful.citymanager.policeService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.vengeful.citymanager.models.police.PoliceRecord
import org.vengeful.citymanager.personService.db.Persons

object PoliceRecords : IntIdTable("police_records") {
    val personId = reference("person_id", Persons.id, onDelete = ReferenceOption.CASCADE)
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val dateOfBirth = long("date_of_birth")
    val workplace = varchar("workplace", 255)
    val photoUrl = varchar("photo_url", 500).nullable()
    val fingerprintNumber = integer("fingerprint_number").nullable()
    val createdAt = long("created_at")
}

class PoliceRecordDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PoliceRecordDao>(PoliceRecords)

    var personId by PoliceRecords.personId
    var firstName by PoliceRecords.firstName
    var lastName by PoliceRecords.lastName
    var dateOfBirth by PoliceRecords.dateOfBirth
    var workplace by PoliceRecords.workplace
    var photoUrl by PoliceRecords.photoUrl
    var fingerprintNumber by PoliceRecords.fingerprintNumber
    var createdAt by PoliceRecords.createdAt

    fun toPoliceRecord() = PoliceRecord(
        id = id.value,
        personId = personId.value,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        workplace = workplace,
        photoUrl = photoUrl,
        fingerprintNumber = fingerprintNumber,
        createdAt = createdAt
    )
}

