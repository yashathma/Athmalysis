package com.cs407.tickertock.data

/**
 * ========================================
 * FAKE DATA PROVIDER - MASTER TOGGLE
 * ========================================
 *
 * TO USE REAL DATA WITH API KEYS:
 * Change: USE_FAKE_DATA = false
 *
 * TO TEST WITHOUT API KEYS:
 * Change: USE_FAKE_DATA = true
 *
 * This provides fake data for all app features:
 * - Stock prices and info
 * - News articles
 * - AI summaries
 * - Sentiment analysis
 * - War Room analytics
 */
object FakeDataProvider {

    /**
     * MASTER TOGGLE - Change this one variable to switch between fake and real data
     */
    const val USE_FAKE_DATA = true  // Set to false to use real API data

    // ==================== WATCHLIST STOCKS ====================

    fun getWatchlistStocks(): List<String> {
        return if (USE_FAKE_DATA) {
            listOf("NVDA", "AAPL", "TSLA")
        } else {
            emptyList() // Will be populated from real data
        }
    }

    // ==================== STOCK DATA ====================

    fun getStockData(): Map<String, Stock> {
        return if (USE_FAKE_DATA) {
            mapOf(
                "NVDA" to Stock(
                    symbol = "NVDA",
                    name = "NVIDIA Corporation",
                    currentPrice = 195.21,
                    priceChange = -3.48,
                    percentageChange = -1.75
                ),
                "AAPL" to Stock(
                    symbol = "AAPL",
                    name = "Apple Inc.",
                    currentPrice = 178.45,
                    priceChange = 2.34,
                    percentageChange = 1.33
                ),
                "TSLA" to Stock(
                    symbol = "TSLA",
                    name = "Tesla, Inc.",
                    currentPrice = 242.84,
                    priceChange = 5.67,
                    percentageChange = 2.39
                )
            )
        } else {
            emptyMap() // Will be populated from real API
        }
    }

    // ==================== NEWS ARTICLES ====================

    fun getNewsArticles(): Map<String, List<NewsArticle>> {
        return if (USE_FAKE_DATA) {
            mapOf(
                "NVDA" to listOf(
                    NewsArticle(
                        id = "nvda_1",
                        title = "Jensen Huang Discusses AI Chip Demand at Tech Conference",
                        summary = "NVIDIA CEO highlighted unprecedented demand for AI accelerators, with data centers expanding globally. The company's latest GPU architecture shows significant performance improvements.",
                        publishedAt = "2 hours ago",
                        publisher = "Tech News Daily",
                        stockSymbol = "NVDA"
                    ),
                    NewsArticle(
                        id = "nvda_2",
                        title = "NVIDIA Partners with Major Cloud Providers",
                        summary = "Strategic partnerships announced with leading cloud infrastructure companies to deploy next-generation AI computing solutions across enterprise markets.",
                        publishedAt = "5 hours ago",
                        publisher = "Financial Times",
                        stockSymbol = "NVDA"
                    ),
                    NewsArticle(
                        id = "nvda_3",
                        title = "Q4 Earnings Beat Analyst Expectations",
                        summary = "Strong quarterly results driven by data center revenue growth. Management raised guidance for the coming year citing robust AI adoption trends.",
                        publishedAt = "1 day ago",
                        publisher = "Bloomberg",
                        stockSymbol = "NVDA"
                    )
                ),
                "AAPL" to listOf(
                    NewsArticle(
                        id = "aapl_1",
                        title = "Apple Unveils New iPhone Features",
                        summary = "Latest iPhone model introduces advanced AI capabilities and improved camera systems, positioning for strong holiday sales.",
                        publishedAt = "3 hours ago",
                        publisher = "The Verge",
                        stockSymbol = "AAPL"
                    ),
                    NewsArticle(
                        id = "aapl_2",
                        title = "Services Revenue Reaches All-Time High",
                        summary = "Apple's services segment continues strong growth trajectory with record subscriptions across App Store, iCloud, and Apple Music.",
                        publishedAt = "6 hours ago",
                        publisher = "CNBC",
                        stockSymbol = "AAPL"
                    )
                ),
                "TSLA" to listOf(
                    NewsArticle(
                        id = "tsla_1",
                        title = "Tesla Cybertruck Deliveries Exceed Projections",
                        summary = "Early delivery numbers surpass analyst estimates as production ramps up at new manufacturing facilities.",
                        publishedAt = "4 hours ago",
                        publisher = "Reuters",
                        stockSymbol = "TSLA"
                    ),
                    NewsArticle(
                        id = "tsla_2",
                        title = "Full Self-Driving Beta Expands to New Markets",
                        summary = "Tesla's autonomous driving software rolls out to additional regions following regulatory approvals.",
                        publishedAt = "8 hours ago",
                        publisher = "Electrek",
                        stockSymbol = "TSLA"
                    )
                )
            )
        } else {
            emptyMap() // Will be populated from real API
        }
    }

    // ==================== AI SUMMARIES ====================

    fun getAISummaries(): Map<String, AISummary> {
        return if (USE_FAKE_DATA) {
            mapOf(
                "NVDA" to AISummary(
                    stockSymbol = "NVDA",
                    summary = "NVIDIA continues to dominate the AI chip market with strong demand from data centers and cloud providers. Recent partnerships and earnings beat demonstrate solid fundamentals and market positioning.",
                    keyPoints = listOf(
                        "AI chip demand remains exceptionally strong",
                        "Strategic cloud partnerships expanding market reach",
                        "Q4 earnings exceeded analyst expectations",
                        "Data center revenue growth accelerating"
                    ),
                    sentiment = "Bullish",
                    generatedAt = "2025-11-05 10:30 AM"
                ),
                "AAPL" to AISummary(
                    stockSymbol = "AAPL",
                    summary = "Apple shows resilient performance with new product launches and record services revenue. The ecosystem continues to drive customer retention and monetization opportunities.",
                    keyPoints = listOf(
                        "New iPhone features driving upgrade cycle",
                        "Services revenue at all-time high",
                        "Strong ecosystem lock-in effects",
                        "Holiday season outlook positive"
                    ),
                    sentiment = "Bullish",
                    generatedAt = "2025-11-05 10:30 AM"
                ),
                "TSLA" to AISummary(
                    stockSymbol = "TSLA",
                    summary = "Tesla demonstrates execution on Cybertruck production ramp while expanding autonomous driving capabilities. Product diversification and technology leadership remain key strengths.",
                    keyPoints = listOf(
                        "Cybertruck deliveries exceeding expectations",
                        "FSD expansion into new markets",
                        "Production efficiency improving",
                        "Technology leadership maintained"
                    ),
                    sentiment = "Bullish",
                    generatedAt = "2025-11-05 10:30 AM"
                )
            )
        } else {
            emptyMap() // Will be generated from real news via API
        }
    }

    // ==================== SENTIMENT DATA ====================

    fun getSentimentData(): Map<String, StockSentiment> {
        return if (USE_FAKE_DATA) {
            mapOf(
                "NVDA" to StockSentiment(
                    stockSymbol = "NVDA",
                    sentimentScore = 0.75,
                    sentimentLabel = "Bullish",
                    articleCount = 45,
                    lastUpdated = System.currentTimeMillis(),
                    supportingArticles = listOf(
                        SentimentArticle(
                            title = "AI Chip Demand Soars",
                            publisher = "TechCrunch",
                            publishedAt = "2 hours ago",
                            sentimentScore = 0.8,
                            sentimentLabel = "Very Bullish",
                            relevanceScore = 0.95,
                            summary = "Data centers rushing to acquire NVIDIA GPUs",
                            url = null
                        )
                    )
                ),
                "AAPL" to StockSentiment(
                    stockSymbol = "AAPL",
                    sentimentScore = 0.6,
                    sentimentLabel = "Somewhat-Bullish",
                    articleCount = 38,
                    lastUpdated = System.currentTimeMillis(),
                    supportingArticles = listOf(
                        SentimentArticle(
                            title = "iPhone Sales Strong",
                            publisher = "WSJ",
                            publishedAt = "3 hours ago",
                            sentimentScore = 0.65,
                            sentimentLabel = "Bullish",
                            relevanceScore = 0.9,
                            summary = "New iPhone features driving sales",
                            url = null
                        )
                    )
                ),
                "TSLA" to StockSentiment(
                    stockSymbol = "TSLA",
                    sentimentScore = 0.55,
                    sentimentLabel = "Somewhat-Bullish",
                    articleCount = 52,
                    lastUpdated = System.currentTimeMillis(),
                    supportingArticles = listOf(
                        SentimentArticle(
                            title = "Cybertruck Production Ramps",
                            publisher = "Bloomberg",
                            publishedAt = "4 hours ago",
                            sentimentScore = 0.7,
                            sentimentLabel = "Bullish",
                            relevanceScore = 0.85,
                            summary = "Tesla meeting production targets",
                            url = null
                        )
                    )
                )
            )
        } else {
            emptyMap() // Will be analyzed from real news data
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Get a specific stock by symbol
     */
    fun getStock(symbol: String): Stock? {
        return getStockData()[symbol]
    }

    /**
     * Get news for a specific stock
     */
    fun getNewsForStock(symbol: String): List<NewsArticle> {
        return getNewsArticles()[symbol] ?: emptyList()
    }

    /**
     * Get AI summary for a specific stock
     */
    fun getAISummaryForStock(symbol: String): AISummary? {
        return getAISummaries()[symbol]
    }

    /**
     * Get sentiment for a specific stock
     */
    fun getSentimentForStock(symbol: String): StockSentiment? {
        return getSentimentData()[symbol]
    }
}
