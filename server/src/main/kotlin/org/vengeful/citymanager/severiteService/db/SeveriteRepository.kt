package org.vengeful.citymanager.severiteService.db

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveriteCounts
import org.vengeful.citymanager.models.severite.SeveritePurity
import org.vengeful.citymanager.severiteService.ISeveriteRepository

class SeveriteRepository : ISeveriteRepository {

    override fun addSeverite(purity: SeveritePurity): Severite = transaction {
        val severiteDao = SeveriteDao.new {
            this.purity = purity.name
            this.createdAt = System.currentTimeMillis()
        }
        severiteDao.toSeverite()
    }

    override fun getSeveriteCounts(): SeveriteCounts = transaction {
        val all = SeveriteDao.all().map { it.toSeverite() }
        SeveriteCounts(
            contaminated = all.count { it.purity == SeveritePurity.CONTAMINATED },
            normal = all.count { it.purity == SeveritePurity.NORMAL },
            crystalClear = all.count { it.purity == SeveritePurity.CRYSTAL_CLEAR }
        )
    }

    override fun getAllSeverite(): List<Severite> = transaction {
        SeveriteDao.all().map { it.toSeverite() }
    }

    override fun getSeveriteByIds(ids: List<Int>): List<Severite> = transaction {
        ids.mapNotNull { id ->
            SeveriteDao.findById(id)?.toSeverite()
        }
    }

    override fun deleteSeverite(ids: List<Int>): Boolean = transaction {
        var deleted = 0
        ids.forEach { id ->
            SeveriteDao.findById(id)?.delete()?.let { deleted++ }
        }
        deleted > 0
    }
}



