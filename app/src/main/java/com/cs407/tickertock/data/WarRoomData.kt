package com.cs407.tickertock.data

/**
 * Data models for the War Room Dashboard
 * AI-generated analytics and insights for stock analysis
 */

/**
 * Sentiment analysis data for speedometer gauge
 * @param score Sentiment score from -100 (extremely bearish) to +100 (extremely bullish)
 * @param label Human-readable sentiment label
 * @param confidence AI confidence level (0.0 to 1.0)
 */
data class SentimentAnalysis(
    val score: Float, // -100 to +100
    val label: String, // "Extremely Bearish", "Bearish", "Neutral", "Bullish", "Extremely Bullish"
    val confidence: Float, // 0.0 to 1.0
    val positiveFactors: List<String>,
    val negativeFactors: List<String>
) {
    companion object {
        fun fromNewsArticles(articles: List<NewsArticle>): SentimentAnalysis {
            if (articles.isEmpty()) {
                return SentimentAnalysis(
                    score = 0f,
                    label = "Neutral",
                    confidence = 0.5f,
                    positiveFactors = emptyList(),
                    negativeFactors = emptyList()
                )
            }

            // Simple sentiment analysis based on keywords
            val positiveKeywords = listOf("surge", "gain", "growth", "profit", "beat", "rally", "soar", "success", "strong", "upgrade", "bullish", "optimistic")
            val negativeKeywords = listOf("fall", "drop", "loss", "decline", "miss", "plunge", "weak", "concern", "risk", "downgrade", "bearish", "pessimistic")

            var positiveCount = 0
            var negativeCount = 0
            val positiveFactors = mutableListOf<String>()
            val negativeFactors = mutableListOf<String>()

            articles.forEach { article ->
                val text = "${article.title} ${article.summary}".lowercase()

                positiveKeywords.forEach { keyword ->
                    if (keyword in text) {
                        positiveCount++
                        if (positiveFactors.size < 3) {
                            positiveFactors.add(article.title)
                        }
                    }
                }

                negativeKeywords.forEach { keyword ->
                    if (keyword in text) {
                        negativeCount++
                        if (negativeFactors.size < 3) {
                            negativeFactors.add(article.title)
                        }
                    }
                }
            }

            val totalSignals = positiveCount + negativeCount
            val score = if (totalSignals > 0) {
                ((positiveCount - negativeCount).toFloat() / totalSignals * 100).coerceIn(-100f, 100f)
            } else {
                0f
            }

            val label = when {
                score >= 60 -> "Extremely Bullish"
                score >= 20 -> "Bullish"
                score >= -20 -> "Neutral"
                score >= -60 -> "Bearish"
                else -> "Extremely Bearish"
            }

            val confidence = if (totalSignals > 0) {
                (totalSignals.toFloat() / (articles.size * 2)).coerceIn(0f, 1f)
            } else {
                0.3f
            }

            return SentimentAnalysis(
                score = score,
                label = label,
                confidence = confidence,
                positiveFactors = positiveFactors.distinct(),
                negativeFactors = negativeFactors.distinct()
            )
        }
    }
}

/**
 * Risk assessment across multiple dimensions
 * Each dimension is scored from 0.0 (low risk) to 1.0 (high risk)
 */
data class RiskAssessment(
    val volatilityRisk: Float, // 0.0 to 1.0
    val marketRisk: Float,
    val newsRisk: Float,
    val technicalRisk: Float,
    val sentimentRisk: Float,
    val overallRisk: Float
) {
    val riskLevel: String
        get() = when {
            overallRisk >= 0.75f -> "CRITICAL"
            overallRisk >= 0.5f -> "HIGH"
            overallRisk >= 0.3f -> "MODERATE"
            else -> "LOW"
        }

    companion object {
        fun fromStockData(stock: Stock, articles: List<NewsArticle>): RiskAssessment {
            // Volatility risk based on percentage change
            val volatilityRisk = (kotlin.math.abs(stock.percentageChange.toDouble()) / 10.0).coerceIn(0.0, 1.0).toFloat()

            // Market risk based on price change magnitude
            val marketRisk = (kotlin.math.abs(stock.priceChange.toDouble()) / stock.currentPrice * 20.0).coerceIn(0.0, 1.0).toFloat()

            // News risk based on article volume and sentiment dispersion
            val newsRisk = if (articles.isEmpty()) {
                0.5f // Unknown is risky
            } else {
                val sentimentAnalysis = SentimentAnalysis.fromNewsArticles(articles)
                // Higher risk if sentiment is extreme or confidence is low
                (kotlin.math.abs(sentimentAnalysis.score) / 100f * (1 - sentimentAnalysis.confidence)).coerceIn(0f, 1f)
            }

            // Technical risk based on trend direction vs sentiment
            val sentimentAnalysis = SentimentAnalysis.fromNewsArticles(articles)
            val technicalRisk = if (articles.isNotEmpty()) {
                // Risk is high if price and sentiment disagree
                val pricePositive = stock.priceChange > 0
                val sentimentPositive = sentimentAnalysis.score > 0
                if (pricePositive != sentimentPositive) 0.7f else 0.3f
            } else {
                0.5f
            }

            // Sentiment risk based on mixed signals
            val sentimentRisk = if (articles.isNotEmpty()) {
                1f - sentimentAnalysis.confidence
            } else {
                0.5f
            }

            val overallRisk = (volatilityRisk + marketRisk + newsRisk + technicalRisk + sentimentRisk) / 5f

            return RiskAssessment(
                volatilityRisk = volatilityRisk,
                marketRisk = marketRisk,
                newsRisk = newsRisk,
                technicalRisk = technicalRisk,
                sentimentRisk = sentimentRisk,
                overallRisk = overallRisk
            )
        }
    }
}

/**
 * News impact event for timeline visualization
 */
data class NewsImpact(
    val timestamp: String,
    val title: String,
    val impact: Float, // -1.0 (very negative) to +1.0 (very positive)
    val impactLabel: String, // "Major Negative", "Minor Positive", etc.
    val relativeTime: String // "2 hours ago"
)

/**
 * Price prediction data point
 */
data class PricePrediction(
    val timeLabel: String, // "Now", "1h", "4h", "1d", "1w"
    val predictedPrice: Double,
    val confidence: Float, // 0.0 to 1.0
    val range: PriceRange
) {
    data class PriceRange(
        val low: Double,
        val high: Double
    )
}

/**
 * Complete War Room analysis for a stock
 */
data class WarRoomAnalysis(
    val stockSymbol: String,
    val sentiment: SentimentAnalysis,
    val risk: RiskAssessment,
    val newsImpacts: List<NewsImpact>,
    val pricePredictions: List<PricePrediction>,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun generate(stock: Stock, articles: List<NewsArticle>): WarRoomAnalysis {
            val sentiment = SentimentAnalysis.fromNewsArticles(articles)
            val risk = RiskAssessment.fromStockData(stock, articles)
            val newsImpacts = generateNewsImpacts(articles)
            val pricePredictions = generatePricePredictions(stock, sentiment)

            return WarRoomAnalysis(
                stockSymbol = stock.symbol,
                sentiment = sentiment,
                risk = risk,
                newsImpacts = newsImpacts,
                pricePredictions = pricePredictions
            )
        }

        private fun generateNewsImpacts(articles: List<NewsArticle>): List<NewsImpact> {
            return articles.take(10).map { article ->
                val text = "${article.title} ${article.summary}".lowercase()

                // Calculate impact based on sentiment keywords
                val positiveScore = listOf("surge", "gain", "growth", "profit", "beat", "rally", "soar", "success", "strong", "upgrade")
                    .count { it in text }
                val negativeScore = listOf("fall", "drop", "loss", "decline", "miss", "plunge", "weak", "concern", "risk", "downgrade")
                    .count { it in text }

                val impact = ((positiveScore - negativeScore).toFloat() / 5f).coerceIn(-1f, 1f)

                val impactLabel = when {
                    impact >= 0.6f -> "Major Positive"
                    impact >= 0.2f -> "Minor Positive"
                    impact >= -0.2f -> "Neutral"
                    impact >= -0.6f -> "Minor Negative"
                    else -> "Major Negative"
                }

                NewsImpact(
                    timestamp = article.publishedAt,
                    title = article.title,
                    impact = impact,
                    impactLabel = impactLabel,
                    relativeTime = article.publishedAt
                )
            }
        }

        private fun generatePricePredictions(stock: Stock, sentiment: SentimentAnalysis): List<PricePrediction> {
            val currentPrice = stock.currentPrice
            val trendFactor = stock.percentageChange / 100.0
            val sentimentFactor = sentiment.score / 1000.0 // Small sentiment influence

            return listOf(
                PricePrediction(
                    timeLabel = "Now",
                    predictedPrice = currentPrice,
                    confidence = 1.0f,
                    range = PricePrediction.PriceRange(currentPrice, currentPrice)
                ),
                PricePrediction(
                    timeLabel = "1h",
                    predictedPrice = currentPrice * (1 + trendFactor * 0.1 + sentimentFactor),
                    confidence = 0.85f,
                    range = PricePrediction.PriceRange(
                        currentPrice * 0.995,
                        currentPrice * 1.005
                    )
                ),
                PricePrediction(
                    timeLabel = "4h",
                    predictedPrice = currentPrice * (1 + trendFactor * 0.3 + sentimentFactor * 2),
                    confidence = 0.7f,
                    range = PricePrediction.PriceRange(
                        currentPrice * 0.985,
                        currentPrice * 1.015
                    )
                ),
                PricePrediction(
                    timeLabel = "1d",
                    predictedPrice = currentPrice * (1 + trendFactor * 0.5 + sentimentFactor * 3),
                    confidence = 0.55f,
                    range = PricePrediction.PriceRange(
                        currentPrice * 0.97,
                        currentPrice * 1.03
                    )
                ),
                PricePrediction(
                    timeLabel = "1w",
                    predictedPrice = currentPrice * (1 + trendFactor * 1.5 + sentimentFactor * 5),
                    confidence = 0.4f,
                    range = PricePrediction.PriceRange(
                        currentPrice * 0.92,
                        currentPrice * 1.08
                    )
                )
            )
        }
    }
}
