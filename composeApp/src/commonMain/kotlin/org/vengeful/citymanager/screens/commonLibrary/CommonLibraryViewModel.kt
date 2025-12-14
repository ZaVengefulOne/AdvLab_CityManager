package org.vengeful.citymanager.screens.commonLibrary

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.library.ILibraryInteractor
import org.vengeful.citymanager.models.library.Article

class CommonLibraryViewModel(
    private val libraryInteractor: ILibraryInteractor
) : BaseViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var updateJob: Job? = null

    companion object {
        const val UPDATE_INTERVAL_MS = 3600000L // 1 час
    }

    init {
        loadArticles()
        startPeriodicUpdate()
    }

    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val articlesList = libraryInteractor.getAllArticles()
                _articles.value = articlesList
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки статей: ${e.message}"
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
                loadArticles()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }
}
