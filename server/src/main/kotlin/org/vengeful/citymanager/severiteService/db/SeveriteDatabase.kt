package org.vengeful.citymanager.severiteService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveritePurity

object Severites : IntIdTable("severites") {
    val purity = varchar("purity", 50)
    val createdAt = long("created_at")
}

class SeveriteDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SeveriteDao>(Severites)

    var purity by Severites.purity
    var createdAt by Severites.createdAt

    fun toSeverite() = Severite(
        id = id.value,
        purity = try {
            SeveritePurity.valueOf(purity)
        } catch (e: IllegalArgumentException) {
            SeveritePurity.NORMAL
        },
        createdAt = createdAt
    )
}


