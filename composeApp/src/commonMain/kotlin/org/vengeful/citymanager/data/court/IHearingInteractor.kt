package org.vengeful.citymanager.data.court

import org.vengeful.citymanager.models.court.Hearing

interface IHearingInteractor {
    suspend fun createHearing(hearing: Hearing): Hearing
    suspend fun getAllHearings(): List<Hearing>
    suspend fun getHearingById(id: Int): Hearing?
    suspend fun getHearingByCaseId(caseId: Int): Hearing?
    suspend fun updateHearing(id: Int, hearing: Hearing): Hearing
    suspend fun deleteHearing(id: Int): Boolean
}



