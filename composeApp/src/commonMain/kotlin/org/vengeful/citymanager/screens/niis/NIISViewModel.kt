package org.vengeful.citymanager.screens.niis

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.severite.ISeveriteInteractor
import org.vengeful.citymanager.models.severite.SeveriteCounts

class NIISViewModel(
    private val severiteInteractor: ISeveriteInteractor
) : BaseViewModel() {

    private val _severiteCounts = MutableStateFlow<SeveriteCounts?>(null)
    val severiteCounts: StateFlow<SeveriteCounts?> = _severiteCounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSeveriteCounts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val counts = severiteInteractor.getSeveriteCounts()
                _severiteCounts.value = counts
            } catch (e: Exception) {
                println("Failed to load severite counts: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}


