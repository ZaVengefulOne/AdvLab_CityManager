package org.vengeful.citymanager.models.stocks

data class StockData(
    val config: StockConfig,
    val currentPrice: Double,
    val history: List<Double>,
    val colorIndex: Int
)
