# 🔄 Fake Data Toggle - Quick Reference

## 📍 Location
`app/src/main/java/com/cs407/tickertock/data/FakeDataProvider.kt`

## 🎯 One-Line Change to Switch Between Fake and Real Data

### To Use FAKE Data (No API Key Needed)
```kotlin
const val USE_FAKE_DATA = true
```

### To Use REAL Data (API Key Required)
```kotlin
const val USE_FAKE_DATA = false
```

---

## ✅ What Features Work with Fake Data

When `USE_FAKE_DATA = true`, you can test ALL app features without API calls:

### 1. **Watchlist** (LIST tab)
- ✓ 3 pre-loaded stocks: NVDA, AAPL, TSLA
- ✓ Real-time price data
- ✓ Price changes and percentages

### 2. **News** (NEWS tab)
- ✓ Multiple news articles per stock
- ✓ Swipe functionality
- ✓ Article summaries and publishers
- ✓ Timestamps

### 3. **AI Summary** (AI tab)
- ✓ AI-generated summaries for each stock
- ✓ Key bullet points
- ✓ Sentiment labels
- ✓ Generated timestamps

### 4. **Sentiment Heatmap** (MAP tab)
- ✓ Sentiment scores for all stocks
- ✓ Color-coded sentiment visualization
- ✓ Supporting articles
- ✓ Article counts

### 5. **War Room** (WAR tab)
- ✓ Sentiment Analysis gauge
- ✓ Risk Assessment radar
- ✓ News Impact Timeline (auto-scrolling)
- ✓ Price Predictions graph

---

## 🔧 How It Works

The `FakeDataProvider` object provides all test data:

```kotlin
// Watchlist stocks
FakeDataProvider.getWatchlistStocks()

// Stock price data
FakeDataProvider.getStockData()

// News articles
FakeDataProvider.getNewsArticles()

// AI summaries
FakeDataProvider.getAISummaries()

// Sentiment analysis
FakeDataProvider.getSentimentData()
```

---

## 🚀 Quick Start

1. **Open**: `app/src/main/java/com/cs407/tickertock/data/FakeDataProvider.kt`

2. **Find line 19**:
   ```kotlin
   const val USE_FAKE_DATA = true
   ```

3. **Change to `true` or `false` as needed**

4. **Build and run** - That's it!

---

## 📝 Sample Fake Data Included

### Stocks
- **NVDA** (NVIDIA) - Down 1.75%
- **AAPL** (Apple) - Up 1.33%
- **TSLA** (Tesla) - Up 2.39%

### News Articles
- 3 articles for NVDA
- 2 articles for AAPL
- 2 articles for TSLA

### All have matching:
- AI Summaries
- Sentiment Data
- War Room Analytics

---

## ⚡ Benefits

✅ **Test without API keys**
✅ **Instant app functionality**
✅ **No network delays**
✅ **Consistent data for UI testing**
✅ **One-line toggle to switch to real data**

---

## 🔄 Switching to Real Data

When you have your API key:

1. Set `USE_FAKE_DATA = false`
2. Add your API key to `ApiKeyManager`
3. The app will automatically use real Alpha Vantage data

That's it! No other code changes needed.
