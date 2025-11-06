package com.cs407.tickertock.repository

import com.cs407.tickertock.api.AlphaVantageService
import com.cs407.tickertock.data.StockSentiment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SentimentRepository {
    private val apiService = AlphaVantageService.getInstance()

    // Cache for sentiment data
    private val sentimentCache = mutableMapOf<String, StockSentiment>()
    private val CACHE_DURATION = 15 * 60 * 1000L // 15 minutes

    /**
     * Fetch sentiment data for multiple stocks
     */
    suspend fun fetchSentiments(symbols: List<String>): Result<Map<String, StockSentiment>> {
        return withContext(Dispatchers.IO) {
            try {
                val sentiments = mutableMapOf<String, StockSentiment>()

                for (symbol in symbols) {
                    // Check cache first
                    val cached = sentimentCache[symbol]
                    if (cached != null && !isCacheExpired(cached.lastUpdated)) {
                        sentiments[symbol] = cached
                        continue
                    }

                    // Fetch from API
                    val result = apiService.fetchSentiment(symbol)
                    if (result.isSuccess) {
                        val sentiment = result.getOrThrow()
                        sentimentCache[symbol] = sentiment
                        sentiments[symbol] = sentiment
                    } else {
                        // If one fails, continue with others but log the error
                        // Return cached data if available
                        cached?.let { sentiments[symbol] = it }
                    }
                }

                if (sentiments.isEmpty()) {
                    Result.failure(Exception("Failed to fetch sentiment data for any stock"))
                } else {
                    Result.success(sentiments)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch sentiment for a single stock
     */
    suspend fun fetchSentiment(symbol: String): Result<StockSentiment> {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                val cached = sentimentCache[symbol]
                if (cached != null && !isCacheExpired(cached.lastUpdated)) {
                    return@withContext Result.success(cached)
                }

                // Fetch from API
                val result = apiService.fetchSentiment(symbol)
                if (result.isSuccess) {
                    val sentiment = result.getOrThrow()
                    sentimentCache[symbol] = sentiment
                    Result.success(sentiment)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch sentiment"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get cached sentiment data
     */
    fun getCachedSentiment(symbol: String): StockSentiment? {
        val cached = sentimentCache[symbol]
        return if (cached != null && !isCacheExpired(cached.lastUpdated)) {
            cached
        } else {
            null
        }
    }

    /**
     * Clear cache for a specific stock
     */
    fun clearCache(symbol: String) {
        sentimentCache.remove(symbol)
    }

    /**
     * Clear all cached data
     */
    fun clearAllCache() {
        sentimentCache.clear()
    }

    private fun isCacheExpired(lastUpdated: Long): Boolean {
        return System.currentTimeMillis() - lastUpdated > CACHE_DURATION
    }

    companion object {
        @Volatile
        private var instance: SentimentRepository? = null

        fun getInstance(): SentimentRepository {
            return instance ?: synchronized(this) {
                instance ?: SentimentRepository().also { instance = it }
            }
        }
    }
}
