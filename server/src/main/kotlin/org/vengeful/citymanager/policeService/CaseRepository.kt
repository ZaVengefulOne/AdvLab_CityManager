package org.vengeful.citymanager.policeService

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.policeService.db.CaseDao
import org.vengeful.citymanager.policeService.db.Cases

class CaseRepository {

    fun createCase(case: Case): Case = transaction {
        // Проверяем, что следователь существует
        val investigatorDao = PersonDao.findById(case.investigatorPersonId)
            ?: throw IllegalStateException("Investigator person with id ${case.investigatorPersonId} not found")

        // Проверяем заявителя, если указан ID
        if (case.complainantPersonId != null) {
            PersonDao.findById(case.complainantPersonId!!)
                ?: throw IllegalStateException("Complainant person with id ${case.complainantPersonId} not found")
        }

        // Проверяем подозреваемого, если указан ID
        if (case.suspectPersonId != null) {
            PersonDao.findById(case.suspectPersonId!!)
                ?: throw IllegalStateException("Suspect person with id ${case.suspectPersonId} not found")
        }

        val caseDao = CaseDao.new {
            complainantPersonId = case.complainantPersonId?.let { PersonDao.findById(it)?.id }
            complainantName = case.complainantName
            investigatorPersonId = investigatorDao.id
            suspectPersonId = case.suspectPersonId?.let { PersonDao.findById(it)?.id }
            suspectName = case.suspectName
            statementText = case.statementText
            violationArticle = case.violationArticle
            status = case.status.name
            photoCompositeUrl = case.photoCompositeUrl
            createdAt = case.createdAt
        }

        caseDao.toCase()
    }

    fun getCaseById(id: Int): Case? = transaction {
        CaseDao.findById(id)?.toCase()
    }

    fun getAllCases(): List<Case> = transaction {
        CaseDao.all().map { it.toCase() }
    }

    fun getCasesByInvestigator(personId: Int): List<Case> = transaction {
        CaseDao.find { Cases.investigatorPersonId eq personId }
            .map { it.toCase() }
    }

    fun getCasesBySuspect(personId: Int): List<Case> = transaction {
        CaseDao.find { Cases.suspectPersonId eq personId }
            .map { it.toCase() }
    }

    fun getCasesByComplainant(personId: Int): List<Case> = transaction {
        CaseDao.find { Cases.complainantPersonId eq personId }
            .map { it.toCase() }
    }

    fun updateCase(id: Int, case: Case): Case = transaction {
        val caseDao = CaseDao.findById(id)
            ?: throw IllegalStateException("Case with id $id not found")

        // Проверяем следователя
        val investigatorDao = PersonDao.findById(case.investigatorPersonId)
            ?: throw IllegalStateException("Investigator person with id ${case.investigatorPersonId} not found")

        // Проверяем заявителя, если указан ID
        if (case.complainantPersonId != null) {
            PersonDao.findById(case.complainantPersonId!!)
                ?: throw IllegalStateException("Complainant person with id ${case.complainantPersonId} not found")
        }

        // Проверяем подозреваемого, если указан ID
        if (case.suspectPersonId != null) {
            PersonDao.findById(case.suspectPersonId!!)
                ?: throw IllegalStateException("Suspect person with id ${case.suspectPersonId} not found")
        }

        caseDao.complainantPersonId = case.complainantPersonId?.let { PersonDao.findById(it)?.id }
        caseDao.complainantName = case.complainantName
        caseDao.investigatorPersonId = investigatorDao.id
        caseDao.suspectPersonId = case.suspectPersonId?.let { PersonDao.findById(it)?.id }
        caseDao.suspectName = case.suspectName
        caseDao.statementText = case.statementText
        caseDao.violationArticle = case.violationArticle
        caseDao.status = case.status.name
        caseDao.photoCompositeUrl = case.photoCompositeUrl

        caseDao.toCase()
    }

    fun updateCaseStatus(id: Int, status: CaseStatus): Case = transaction {
        val caseDao = CaseDao.findById(id)
            ?: throw IllegalStateException("Case with id $id not found")

        caseDao.status = status.name
        caseDao.toCase()
    }

    fun deleteCase(id: Int): Boolean = transaction {
        val caseDao = CaseDao.findById(id)
        if (caseDao != null) {
            caseDao.delete()
            true
        } else {
            false
        }
    }
}

