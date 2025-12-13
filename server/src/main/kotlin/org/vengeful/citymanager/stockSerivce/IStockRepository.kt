package org.vengeful.citymanager.stockSerivce

import org.vengeful.citymanager.models.stocks.StockConfig

interface IStockRepository {
    fun getAllStocks(): List<StockConfig>
    fun getStockByName(name: String): StockConfig?
    fun createStock(stock: StockConfig): StockConfig
    fun updateStock(name: String, newPrice: Double): StockConfig
    fun deleteStock(name: String): Boolean
    fun deleteAllStocks()
}
