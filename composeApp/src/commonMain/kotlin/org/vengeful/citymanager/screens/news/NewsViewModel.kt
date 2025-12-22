package org.vengeful.citymanager.screens.news

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.news.INewsInteractor
import org.vengeful.citymanager.models.news.News

class NewsViewModel(
    private val newsInteractor: INewsInteractor
) : BaseViewModel() {

    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news: StateFlow<List<News>> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var updateJob: Job? = null

    companion object {
        const val UPDATE_INTERVAL_MS = 3600000L // 1 час
    }

    init {
        loadNews()
        startPeriodicUpdate()
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val newsList = newsInteractor.getAllNews()
                _news.value = newsList
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки новостей: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MS)
                loadNews()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }
}
