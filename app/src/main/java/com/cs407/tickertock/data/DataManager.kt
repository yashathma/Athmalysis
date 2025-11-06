package com.cs407.tickertock.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages data persistence using SharedPreferences
 * Saves and loads watchlist, stock data, news data, and user interaction state
 */
class DataManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("tickertock_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_WATCHLIST = "watchlist_stocks"
        private const val KEY_STOCK_DATA = "stock_data_map"
        private const val KEY_NEWS_DATA = "news_data_map"
        private const val KEY_SWIPED_ARTICLES = "swiped_articles"
        private const val KEY_ARTICLE_INDEX = "article_index_per_stock"
        private const val KEY_END_MESSAGE_SHOWN = "end_message_shown_stocks"
        private const val KEY_SELECTED_STOCK = "selected_stock"
    }

    /**
     * Save watchlist stocks
     */
    fun saveWatchlist(stocks: List<String>) {
        val json = gson.toJson(stocks)
        sharedPreferences.edit().putString(KEY_WATCHLIST, json).apply()
    }

    /**
     * Load watchlist stocks
     */
    fun loadWatchlist(): List<String> {
        val json = sharedPreferences.getString(KEY_WATCHLIST, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Save stock data map
     */
    fun saveStockData(stockDataMap: Map<String, Stock>) {
        val json = gson.toJson(stockDataMap)
        sharedPreferences.edit().putString(KEY_STOCK_DATA, json).apply()
    }

    /**
     * Load stock data map
     */
    fun loadStockData(): Map<String, Stock> {
        val json = sharedPreferences.getString(KEY_STOCK_DATA, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Stock>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    /**
     * Save news data map
     */
    fun saveNewsData(newsDataMap: Map<String, List<NewsArticle>>) {
        val json = gson.toJson(newsDataMap)
        sharedPreferences.edit().putString(KEY_NEWS_DATA, json).apply()
    }

    /**
     * Load news data map
     */
    fun loadNewsData(): Map<String, List<NewsArticle>> {
        val json = sharedPreferences.getString(KEY_NEWS_DATA, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, List<NewsArticle>>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    /**
     * Save swiped articles
     */
    fun saveSwipedArticles(swipedArticles: Map<String, Set<String>>) {
        val json = gson.toJson(swipedArticles)
        sharedPreferences.edit().putString(KEY_SWIPED_ARTICLES, json).apply()
    }

    /**
     * Load swiped articles
     */
    fun loadSwipedArticles(): Map<String, Set<String>> {
        val json = sharedPreferences.getString(KEY_SWIPED_ARTICLES, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Set<String>>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    /**
     * Save article index per stock
     */
    fun saveArticleIndex(articleIndexPerStock: Map<String, Int>) {
        val json = gson.toJson(articleIndexPerStock)
        sharedPreferences.edit().putString(KEY_ARTICLE_INDEX, json).apply()
    }

    /**
     * Load article index per stock
     */
    fun loadArticleIndex(): Map<String, Int> {
        val json = sharedPreferences.getString(KEY_ARTICLE_INDEX, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    /**
     * Save end message shown stocks
     */
    fun saveEndMessageShown(endMessageShownForStocks: Set<String>) {
        val json = gson.toJson(endMessageShownForStocks)
        sharedPreferences.edit().putString(KEY_END_MESSAGE_SHOWN, json).apply()
    }

    /**
     * Load end message shown stocks
     */
    fun loadEndMessageShown(): Set<String> {
        val json = sharedPreferences.getString(KEY_END_MESSAGE_SHOWN, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }

    /**
     * Save selected stock
     */
    fun saveSelectedStock(selectedStock: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_STOCK, selectedStock).apply()
    }

    /**
     * Load selected stock
     */
    fun loadSelectedStock(): String {
        return sharedPreferences.getString(KEY_SELECTED_STOCK, "") ?: ""
    }

    /**
     * Clear all data
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Clear data for a specific stock
     */
    fun clearStockData(stockSymbol: String) {
        // Load all data
        val stockDataMap = loadStockData().toMutableMap()
        val newsDataMap = loadNewsData().toMutableMap()
        val swipedArticles = loadSwipedArticles().toMutableMap()
        val articleIndex = loadArticleIndex().toMutableMap()
        val endMessageShown = loadEndMessageShown().toMutableSet()

        // Remove stock-specific data
        stockDataMap.remove(stockSymbol)
        newsDataMap.remove(stockSymbol)
        swipedArticles.remove(stockSymbol)
        articleIndex.remove(stockSymbol)
        endMessageShown.remove(stockSymbol)

        // Save updated data
        saveStockData(stockDataMap)
        saveNewsData(newsDataMap)
        saveSwipedArticles(swipedArticles)
        saveArticleIndex(articleIndex)
        saveEndMessageShown(endMessageShown)
    }
}
