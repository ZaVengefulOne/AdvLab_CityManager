package org.vengeful.citymanager.screens.news

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.news.INewsInteractor
import org.vengeful.citymanager.models.news.News

class NewsItemViewModel(
    private val newsInteractor: INewsInteractor
) : BaseViewModel() {

    private val _news = MutableStateFlow<News?>(null)
    val news: StateFlow<News?> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    fun loadNews(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _news.value = newsInteractor.getNewsById(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
