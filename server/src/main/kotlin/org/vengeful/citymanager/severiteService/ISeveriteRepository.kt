package org.vengeful.citymanager.severiteService

import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveriteCounts
import org.vengeful.citymanager.models.severite.SeveritePurity

interface ISeveriteRepository {
    fun addSeverite(purity: SeveritePurity): Severite
    fun getSeveriteCounts(): SeveriteCounts
    fun getAllSeverite(): List<Severite>
    fun getSeveriteByIds(ids: List<Int>): List<Severite>
    fun deleteSeverite(ids: List<Int>): Boolean
}

