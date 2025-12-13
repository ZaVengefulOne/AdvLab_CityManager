package org.vengeful.citymanager.stockSerivce.db

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.stocks.StockConfig
import org.vengeful.citymanager.stockSerivce.IStockRepository

class StockRepository: IStockRepository {

    override fun getAllStocks(): List<StockConfig> = transaction {
        StockDao.all().map { it.toStockConfig() }
    }

    override fun getStockByName(name: String): StockConfig? = transaction {
        StockDao.find { Stocks.name eq name }
            .firstOrNull()?.toStockConfig()
    }

    override fun createStock(stock: StockConfig): StockConfig = transaction {
        val stockDao = StockDao.new {
            name = stock.name
            averagePrice = stock.averagePrice
        }
        stockDao.toStockConfig()
    }

    override fun updateStock(name: String, newPrice: Double): StockConfig = transaction {
        val stockDao = StockDao.find { Stocks.name eq name }
            .firstOrNull()
            ?: throw IllegalStateException("Stock with name $name not found")
        stockDao.averagePrice = newPrice
        stockDao.toStockConfig()
    }

    override fun deleteStock(name: String): Boolean = transaction {
        StockDao.find { Stocks.name eq name }
            .firstOrNull()?.delete() != null
    }

    override fun deleteAllStocks() = transaction {
        StockDao.all().forEach { it.delete() }
    }
}
