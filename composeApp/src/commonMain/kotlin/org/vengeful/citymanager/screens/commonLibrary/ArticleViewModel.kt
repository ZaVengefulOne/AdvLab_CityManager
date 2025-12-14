package org.vengeful.citymanager.screens.commonLibrary

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.library.ILibraryInteractor
import org.vengeful.citymanager.models.library.Article

class ArticleViewModel(
    private val libraryInteractor: ILibraryInteractor
) : BaseViewModel() {


    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadArticle(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _article.value = libraryInteractor.getArticleById(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
