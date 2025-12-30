package org.vengeful.citymanager.courtService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.policeService.db.Cases

object Hearings : IntIdTable("hearings") {
    val caseId = reference("case_id", Cases.id, onDelete = ReferenceOption.CASCADE)
    val plaintiffPersonId = reference("plaintiff_person_id", Persons.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val plaintiffName = varchar("plaintiff_name", 255)
    val protocol = text("protocol")
    val verdict = varchar("verdict", 500)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

class HearingDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HearingDao>(Hearings)

    var caseId by Hearings.caseId
    var plaintiffPersonId by Hearings.plaintiffPersonId
    var plaintiffName by Hearings.plaintiffName
    var protocol by Hearings.protocol
    var verdict by Hearings.verdict
    var createdAt by Hearings.createdAt
    var updatedAt by Hearings.updatedAt

    fun toHearing() = Hearing(
        id = id.value,
        caseId = caseId.value,
        plaintiffPersonId = plaintiffPersonId?.value,
        plaintiffName = plaintiffName,
        protocol = protocol,
        verdict = verdict,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

