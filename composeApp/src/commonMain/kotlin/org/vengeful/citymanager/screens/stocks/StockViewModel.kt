package org.vengeful.citymanager.screens.stocks

import org.vengeful.citymanager.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.models.stocks.StockConfig
import org.vengeful.citymanager.models.stocks.StockData
import kotlin.random.Random

class StockViewModel(
    private val administrationInteractor: IAdministrationInteractor
) : BaseViewModel() {

    private val _stocks = MutableStateFlow<Map<String, StockData>>(emptyMap())
    val stocks: StateFlow<Map<String, StockData>> = _stocks.asStateFlow()

    private var configUpdateJob: Job? = null
    private val updateJobs = mutableMapOf<String, Job>()
    private val basePrices = mutableMapOf<String, Double>()
    private var stockColorIndices = mutableMapOf<String, Int>()
    private var nextColorIndex = 0

    companion object {
        //        const val UPDATE_INTERVAL_MS = 120000L // 120 секунд TODO: Продовые значения
//        const val CONFIG_UPDATE_INTERVAL_MS = 600000L // 600 секунд TODO: Продовые значения
        const val UPDATE_INTERVAL_MS = 3000L
        const val CONFIG_UPDATE_INTERVAL_MS = 10000L
        const val FLUCTUATION_MIN = -20
        const val FLUCTUATION_MAX = 20
        const val GRAPH_HISTORY_SIZE = 20
    }

    fun loadStocks() {
        viewModelScope.launch {
            try {
                val config = administrationInteractor.getAdministrationConfig()
                updateStocksFromConfig(config.stocks)

                if (configUpdateJob == null || !configUpdateJob!!.isActive) {
                    startConfigUpdates()
                }
            } catch (e: Exception) {
                println("Error loading stocks: ${e.message}")
            }
        }
    }

    private fun updateStocksFromConfig(stocksConfig: List<StockConfig>) {
        val currentStocks = _stocks.value.toMutableMap()

        // Обновляем существующие и добавляем новые
        stocksConfig.forEach { stockConfig ->
            basePrices[stockConfig.name] = stockConfig.averagePrice

            if (!currentStocks.containsKey(stockConfig.name)) {
                // Новая акция - назначаем цвет
                val colorIndex = nextColorIndex
                stockColorIndices[stockConfig.name] = colorIndex
                nextColorIndex = (nextColorIndex + 1) % 7  // 7 цветов в массиве

                currentStocks[stockConfig.name] = StockData(
                    config = stockConfig,
                    currentPrice = stockConfig.averagePrice,
                    history = List(GRAPH_HISTORY_SIZE) { stockConfig.averagePrice },
                    colorIndex = colorIndex  // Добавьте это
                )
                startAutoUpdate(stockConfig.name)
            } else {
                // Обновляем базовую цену существующей акции
                val existing = currentStocks[stockConfig.name]!!
                currentStocks[stockConfig.name] = existing.copy(
                    config = stockConfig
                )
            }
        }

        // Удаляем акции, которых больше нет в конфиге
        val configNames = stocksConfig.map { it.name }.toSet()
        val toRemove = currentStocks.keys.filter { it !in configNames }
        toRemove.forEach { name ->
            updateJobs[name]?.cancel()
            updateJobs.remove(name)
            basePrices.remove(name)
            stockColorIndices.remove(name)  // Удаляем индекс цвета
            currentStocks.remove(name)
        }

        _stocks.value = currentStocks
    }

    private fun startConfigUpdates() {
        configUpdateJob?.cancel()
        configUpdateJob = viewModelScope.launch {
            while (true) {
                delay(CONFIG_UPDATE_INTERVAL_MS)
                try {
                    val config = administrationInteractor.getAdministrationConfig()
                    updateStocksFromConfig(config.stocks)
                } catch (e: Exception) {
                    println("Error updating stocks config: ${e.message}")
                }
            }
        }
    }

    private fun startAutoUpdate(stockName: String) {
        updateJobs[stockName]?.cancel()
        updateJobs[stockName] = viewModelScope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MS)
                val basePrice = basePrices[stockName] ?: return@launch

                val fluctuation = Random.nextDouble(
                    FLUCTUATION_MIN.toDouble(),
                    FLUCTUATION_MAX.toDouble()
                )
                val newPrice = basePrice + fluctuation

                val currentStocks = _stocks.value.toMutableMap()
                val stockData = currentStocks[stockName] ?: return@launch

                // Обновляем цену и историю
                val currentHistory = stockData.history.toMutableList()
                currentHistory.add(newPrice)
                if (currentHistory.size > GRAPH_HISTORY_SIZE) {
                    currentHistory.removeAt(0)
                }

                currentStocks[stockName] = stockData.copy(
                    currentPrice = newPrice,
                    history = currentHistory
                )
                _stocks.value = currentStocks
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        configUpdateJob?.cancel()
        updateJobs.values.forEach { it.cancel() }
        updateJobs.clear()
    }
}
