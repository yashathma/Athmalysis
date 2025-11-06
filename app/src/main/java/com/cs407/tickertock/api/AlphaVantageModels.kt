package com.cs407.tickertock.api

import com.google.gson.annotations.SerializedName

// Global Quote Response
data class GlobalQuoteResponse(
    @SerializedName("Global Quote")
    val globalQuote: GlobalQuote?,
    @SerializedName("Information")
    val information: String?,
    @SerializedName("Note")
    val note: String?
)

data class GlobalQuote(
    @SerializedName("01. symbol")
    val symbol: String,
    @SerializedName("05. price")
    val price: String,
    @SerializedName("09. change")
    val change: String,
    @SerializedName("10. change percent")
    val changePercent: String
)

// News Sentiment Response
data class NewsSentimentResponse(
    val feed: List<NewsItem>?,
    val items: String?,
    @SerializedName("Information")
    val information: String?,
    @SerializedName("Note")
    val note: String?
)

data class NewsItem(
    val title: String?,
    @SerializedName("time_published")
    val timePublished: String?,
    val summary: String?,
    val source: String?,
    val url: String?,
    @SerializedName("overall_sentiment_score")
    val overallSentimentScore: Double?,
    @SerializedName("overall_sentiment_label")
    val overallSentimentLabel: String?,
    @SerializedName("ticker_sentiment")
    val tickerSentiment: List<TickerSentiment>?
)

data class TickerSentiment(
    val ticker: String?,
    @SerializedName("relevance_score")
    val relevanceScore: String?,
    @SerializedName("ticker_sentiment_score")
    val tickerSentimentScore: String?,
    @SerializedName("ticker_sentiment_label")
    val tickerSentimentLabel: String?
)
