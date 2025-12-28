package org.vengeful.citymanager.data.severite

import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveriteCounts
import org.vengeful.citymanager.models.severite.SeveritePurity
import org.vengeful.citymanager.models.severite.SellSeveriteResult

interface ISeveriteInteractor {
    suspend fun addSeverite(purity: SeveritePurity): Severite
    suspend fun getSeveriteCounts(): SeveriteCounts
    suspend fun getAllSeverite(): List<Severite>
    suspend fun sellSeverite(severiteIds: List<Int>): SellSeveriteResult
}

