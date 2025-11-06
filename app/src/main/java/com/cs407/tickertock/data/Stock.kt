package com.cs407.tickertock.data

data class Stock(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChange: Double,
    val percentageChange: Double
) {
    val isPositive: Boolean get() = priceChange >= 0
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val publishedAt: String,
    val publisher: String,
    val stockSymbol: String,
    val isSelected: Boolean = false
)

data class AISummary(
    val stockSymbol: String,
    val summary: String,
    val keyPoints: List<String>,
    val sentiment: String,
    val generatedAt: String
)

data class StockSentiment(
    val stockSymbol: String,
    val sentimentScore: Double, // Range: -1.0 (very negative) to 1.0 (very positive)
    val sentimentLabel: String, // "Bullish", "Somewhat-Bullish", "Neutral", "Somewhat-Bearish", "Bearish"
    val articleCount: Int,
    val lastUpdated: Long,
    val supportingArticles: List<SentimentArticle>
)

data class SentimentArticle(
    val title: String,
    val publisher: String,
    val publishedAt: String,
    val sentimentScore: Double,
    val sentimentLabel: String,
    val relevanceScore: Double,
    val summary: String,
    val url: String?
)