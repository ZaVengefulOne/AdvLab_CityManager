package org.vengeful.citymanager.policeService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.personService.db.Persons

object Cases : IntIdTable("cases") {
    val complainantPersonId = reference("complainant_person_id", Persons.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val complainantName = varchar("complainant_name", 255)
    val investigatorPersonId = reference("investigator_person_id", Persons.id, onDelete = ReferenceOption.CASCADE)
    val suspectPersonId = reference("suspect_person_id", Persons.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val suspectName = varchar("suspect_name", 255)
    val statementText = text("statement_text")
    val violationArticle = varchar("violation_article", 255)
    val status = varchar("status", 50)
    val photoCompositeUrl = varchar("photo_composite_url", 500).nullable()
    val createdAt = long("created_at")
}

class CaseDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CaseDao>(Cases)

    var complainantPersonId by Cases.complainantPersonId
    var complainantName by Cases.complainantName
    var investigatorPersonId by Cases.investigatorPersonId
    var suspectPersonId by Cases.suspectPersonId
    var suspectName by Cases.suspectName
    var statementText by Cases.statementText
    var violationArticle by Cases.violationArticle
    var status by Cases.status
    var photoCompositeUrl by Cases.photoCompositeUrl
    var createdAt by Cases.createdAt

    fun toCase() = Case(
        id = id.value,
        complainantPersonId = complainantPersonId?.value,
        complainantName = complainantName,
        investigatorPersonId = investigatorPersonId.value,
        suspectPersonId = suspectPersonId?.value,
        suspectName = suspectName,
        statementText = statementText,
        violationArticle = violationArticle,
        status = CaseStatus.valueOf(status),
        photoCompositeUrl = photoCompositeUrl,
        createdAt = createdAt
    )
}

