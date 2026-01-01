package org.vengeful.citymanager.courtService

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.courtService.db.HearingDao
import org.vengeful.citymanager.courtService.db.Hearings
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.policeService.db.CaseDao

class HearingRepository {

    fun createHearing(hearing: Hearing): Hearing = transaction {
        // Проверяем, что дело существует
        val caseDao = CaseDao.findById(hearing.caseId)
            ?: throw IllegalStateException("Case with id ${hearing.caseId} not found")

        // Проверяем истца, если указан ID
        if (hearing.plaintiffPersonId != null) {
            PersonDao.findById(hearing.plaintiffPersonId!!)
                ?: throw IllegalStateException("Plaintiff person with id ${hearing.plaintiffPersonId} not found")
        }

        val hearingDao = HearingDao.new {
            caseId = caseDao.id
            plaintiffPersonId = hearing.plaintiffPersonId?.let { PersonDao.findById(it)?.id }
            plaintiffName = hearing.plaintiffName
            protocol = hearing.protocol
            verdict = hearing.verdict
            createdAt = hearing.createdAt
            updatedAt = hearing.updatedAt
        }

        hearingDao.toHearing()
    }

    fun getHearingById(id: Int): Hearing? = transaction {
        HearingDao.findById(id)?.toHearing()
    }

    fun getAllHearings(): List<Hearing> = transaction {
        HearingDao.all().map { it.toHearing() }
    }

    fun getHearingByCaseId(caseId: Int): Hearing? = transaction {
        HearingDao.find { Hearings.caseId eq caseId }.firstOrNull()?.toHearing()
    }

    fun updateHearing(id: Int, hearing: Hearing): Hearing = transaction {
        val hearingDao = HearingDao.findById(id)
            ?: throw IllegalStateException("Hearing with id $id not found")

        // Проверяем, что дело существует
        val caseDao = CaseDao.findById(hearing.caseId)
            ?: throw IllegalStateException("Case with id ${hearing.caseId} not found")

        // Проверяем истца, если указан ID
        if (hearing.plaintiffPersonId != null) {
            PersonDao.findById(hearing.plaintiffPersonId!!)
                ?: throw IllegalStateException("Plaintiff person with id ${hearing.plaintiffPersonId} not found")
        }

        hearingDao.caseId = caseDao.id
        hearingDao.plaintiffPersonId = hearing.plaintiffPersonId?.let { PersonDao.findById(it)?.id }
        hearingDao.plaintiffName = hearing.plaintiffName
        hearingDao.protocol = hearing.protocol
        hearingDao.verdict = hearing.verdict
        hearingDao.updatedAt = hearing.updatedAt

        hearingDao.toHearing()
    }

    fun deleteHearing(id: Int): Boolean = transaction {
        val hearingDao = HearingDao.findById(id)
        if (hearingDao != null) {
            hearingDao.delete()
            true
        } else {
            false
        }
    }
}


