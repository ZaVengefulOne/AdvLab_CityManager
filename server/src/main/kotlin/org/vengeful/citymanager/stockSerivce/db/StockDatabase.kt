package org.vengeful.citymanager.stockSerivce.db


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.vengeful.citymanager.models.stocks.StockConfig

object Stocks : IntIdTable("stocks") {
    val name = varchar("name", 255).uniqueIndex()
    val averagePrice = double("average_price")
}

class StockDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StockDao>(Stocks)

    var name by Stocks.name
    var averagePrice by Stocks.averagePrice

    fun toStockConfig() = StockConfig(
        name = name,
        averagePrice = averagePrice
    )
}
