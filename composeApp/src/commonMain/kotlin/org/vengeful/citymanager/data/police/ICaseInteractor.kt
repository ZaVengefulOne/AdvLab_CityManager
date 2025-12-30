package org.vengeful.citymanager.data.police

import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus

interface ICaseInteractor {
    suspend fun createCase(case: Case, photoBytes: ByteArray?): Case
    suspend fun getAllCases(): List<Case>
    suspend fun getCaseById(caseId: Int): Case?
    suspend fun getCasesBySuspect(personId: Int): List<Case>
    suspend fun getCasesByInvestigator(personId: Int): List<Case>
    suspend fun updateCase(caseId: Int, case: Case, photoBytes: ByteArray?): Case
    suspend fun updateCaseStatus(caseId: Int, status: CaseStatus): Case
    suspend fun deleteCase(caseId: Int): Boolean
}

