package com.cs407.tickertock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.tickertock.data.StockSentiment
import com.cs407.tickertock.repository.SentimentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HeatmapUiState(
    val sentiments: Map<String, StockSentiment> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedStock: String? = null
)

class SentimentHeatmapViewModel : ViewModel() {
    private val repository = SentimentRepository.getInstance()

    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    /**
     * Load sentiment data for watchlist stocks
     */
    fun loadSentiments(symbols: List<String>) {
        if (symbols.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                sentiments = emptyMap(),
                isLoading = false,
                errorMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.fetchSentiments(symbols)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    sentiments = result.getOrThrow(),
                    isLoading = false,
                    errorMessage = null
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (errorMsg.contains("rate limit")) {
                        "API limit reached. Please try again later."
                    } else {
                        "Failed to load sentiment data: $errorMsg"
                    }
                )
            }
        }
    }

    /**
     * Refresh sentiment data
     */
    fun refreshSentiments(symbols: List<String>) {
        if (symbols.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            // Clear cache to force refresh
            repository.clearAllCache()

            val result = repository.fetchSentiments(symbols)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    sentiments = result.getOrThrow(),
                    isRefreshing = false,
                    errorMessage = null
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = if (errorMsg.contains("rate limit")) {
                        "API limit reached. Please try again later."
                    } else {
                        "Failed to refresh: $errorMsg"
                    }
                )
            }
        }
    }

    /**
     * Select a stock for detailed view
     */
    fun selectStock(symbol: String) {
        _uiState.value = _uiState.value.copy(selectedStock = symbol)
    }

    /**
     * Deselect stock
     */
    fun deselectStock() {
        _uiState.value = _uiState.value.copy(selectedStock = null)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
