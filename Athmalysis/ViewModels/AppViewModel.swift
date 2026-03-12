import SwiftUI

@MainActor
class AppViewModel: ObservableObject {
    // MARK: - Navigation Paths
    @Published var watchlistNavPath = NavigationPath()
    @Published var newsNavPath = NavigationPath()
    @Published var aiNavPath = NavigationPath()

    // MARK: - Core State
    @Published var selectedStock: String {
        didSet { DataManager.shared.saveSelectedStock(selectedStock) }
    }
    @Published var watchlistStocks: [String] {
        didSet { DataManager.shared.saveWatchlist(watchlistStocks) }
    }
    @Published var stockDataMap: [String: Stock] {
        didSet { DataManager.shared.saveStockData(stockDataMap) }
    }
    @Published var newsDataMap: [String: [NewsArticle]] {
        didSet { DataManager.shared.saveNewsData(newsDataMap) }
    }

    // MARK: - Article Tracking State
    @Published var swipedArticles: [String: Set<String>] {
        didSet { DataManager.shared.saveSwipedArticles(swipedArticles) }
    }
    /// Full article objects for articles swiped right — persists independently of newsDataMap refreshes
    @Published var savedArticles: [String: [NewsArticle]] {
        didSet { DataManager.shared.saveSavedArticles(savedArticles) }
    }
    @Published var articleIndexPerStock: [String: Int] {
        didSet { DataManager.shared.saveArticleIndex(articleIndexPerStock) }
    }
    @Published var endMessageShownForStocks: Set<String> {
        didSet { DataManager.shared.saveEndMessageShown(endMessageShownForStocks) }
    }
    @Published var closedStocks: Set<String> {
        didSet { DataManager.shared.saveClosedStocks(closedStocks) }
    }

    // MARK: - Fetch Tracking
    @Published var lastNewsFetchDate: [String: Date] {
        didSet { DataManager.shared.saveLastNewsFetchDate(lastNewsFetchDate) }
    }
    @Published var lastPriceFetchDate: Date? {
        didSet { DataManager.shared.saveLastPriceFetchDate(lastPriceFetchDate) }
    }

    // MARK: - Loading State
    @Published var isLoadingStock = false
    @Published var isRefreshing = false
    @Published var errorMessage: String? = nil

    private let yahooService = YahooFinanceService.shared
    private let newsService = AlphaVantageService.shared
    private var refreshTimer: Timer?

    // MARK: - Init

    init() {
        let dm = DataManager.shared
        self.selectedStock = dm.loadSelectedStock()
        self.watchlistStocks = dm.loadWatchlist()
        self.stockDataMap = dm.loadStockData()
        self.newsDataMap = dm.loadNewsData()
        self.swipedArticles = dm.loadSwipedArticles()
        self.savedArticles = dm.loadSavedArticles()
        self.articleIndexPerStock = dm.loadArticleIndex()
        self.endMessageShownForStocks = dm.loadEndMessageShown()
        self.closedStocks = dm.loadClosedStocks()
        self.lastNewsFetchDate = dm.loadLastNewsFetchDate()
        self.lastPriceFetchDate = dm.loadLastPriceFetchDate()
    }

    // MARK: - Actions

    func addStock(_ symbol: String, name: String? = nil) {
        guard !watchlistStocks.contains(symbol) else { return }

        isLoadingStock = true
        errorMessage = nil

        Task {
            do {
                // Fetch price from Yahoo Finance
                let stock = try await yahooService.fetchStockData(symbol: symbol, name: name)

                // Fetch news from Alpha Vantage
                let news = try await newsService.fetchNews(symbol: symbol)

                if news.isEmpty {
                    throw ServiceError.noNews(symbol)
                }

                watchlistStocks.append(symbol)
                stockDataMap[symbol] = stock
                newsDataMap[symbol] = news
                lastNewsFetchDate[symbol] = Date()
                lastPriceFetchDate = Date()
                if selectedStock.isEmpty {
                    selectedStock = symbol
                }
                watchlistNavPath = NavigationPath()
            } catch {
                let msg = error.localizedDescription
                if msg.contains("rate limit") {
                    errorMessage = "API key limit reached. Please try again later."
                } else if msg.contains("No news") {
                    errorMessage = "No news available for \(symbol)"
                } else {
                    errorMessage = "Failed to add stock: \(msg)"
                }
            }
            isLoadingStock = false
        }
    }

    func removeStock(_ symbol: String) {
        watchlistStocks.removeAll { $0 == symbol }
        swipedArticles.removeValue(forKey: symbol)
        savedArticles.removeValue(forKey: symbol)
        articleIndexPerStock.removeValue(forKey: symbol)
        endMessageShownForStocks.remove(symbol)
        closedStocks.remove(symbol)
        stockDataMap.removeValue(forKey: symbol)
        newsDataMap.removeValue(forKey: symbol)
        lastNewsFetchDate.removeValue(forKey: symbol)
        DataManager.shared.clearStockData(symbol)

        if selectedStock == symbol {
            selectedStock = watchlistStocks.first ?? ""
        }
    }

    func moveStock(from source: Int, to destination: Int) {
        guard source != destination,
              source >= 0, source < watchlistStocks.count,
              destination >= 0, destination < watchlistStocks.count else { return }

        let movedStock = watchlistStocks.remove(at: source)
        watchlistStocks.insert(movedStock, at: destination)
    }

    func refreshPrices() {
        guard !watchlistStocks.isEmpty else { return }

        isRefreshing = true
        errorMessage = nil

        Task {
            do {
                let stocks = try await yahooService.refreshStockPrices(
                    symbols: watchlistStocks,
                    stockDataMap: stockDataMap
                )
                for stock in stocks {
                    stockDataMap[stock.symbol] = stock
                }
                lastPriceFetchDate = Date()
            } catch {
                errorMessage = "Failed to refresh: \(error.localizedDescription)"
            }
            isRefreshing = false
        }
    }

    // MARK: - Auto-Refresh

    private var isUSMarketOpen: Bool {
        let now = Date()
        let eastern = TimeZone(identifier: "America/New_York")!
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = eastern

        let weekday = calendar.component(.weekday, from: now)
        // 1 = Sunday, 7 = Saturday
        guard weekday >= 2 && weekday <= 6 else { return false }

        let hour = calendar.component(.hour, from: now)
        let minute = calendar.component(.minute, from: now)
        let totalMinutes = hour * 60 + minute
        // 9:30 AM = 570 min, 4:00 PM = 960 min
        return totalMinutes >= 570 && totalMinutes <= 960
    }

    func startAutoRefresh() {
        stopAutoRefresh()
        refreshTimer = Timer.scheduledTimer(withTimeInterval: 20, repeats: true) { [weak self] _ in
            guard let self else { return }
            Task { @MainActor in
                guard !self.watchlistStocks.isEmpty, self.isUSMarketOpen else { return }
                self.silentRefreshPrices()
            }
        }
    }

    func stopAutoRefresh() {
        refreshTimer?.invalidate()
        refreshTimer = nil
    }

    private func silentRefreshPrices() {
        guard !watchlistStocks.isEmpty else { return }
        Task {
            do {
                let stocks = try await yahooService.refreshStockPrices(
                    symbols: watchlistStocks,
                    stockDataMap: stockDataMap
                )
                for stock in stocks {
                    stockDataMap[stock.symbol] = stock
                }
                lastPriceFetchDate = Date()
            } catch {
                // Silent refresh — don't show errors to user
            }
        }
    }

    func swipeArticle(stockSymbol: String, article: NewsArticle) {
        // Track ID for filtering future fetches
        var currentIds = swipedArticles[stockSymbol] ?? []
        currentIds.insert(article.id)
        swipedArticles[stockSymbol] = currentIds

        // Persist full article object so Summary page survives news refreshes
        var currentSaved = savedArticles[stockSymbol] ?? []
        if !currentSaved.contains(where: { $0.id == article.id }) {
            currentSaved.append(article)
        }
        savedArticles[stockSymbol] = currentSaved
    }

    func setArticleIndex(stockSymbol: String, index: Int) {
        articleIndexPerStock[stockSymbol] = index
    }

    func markEndMessageShown(stockSymbol: String) {
        endMessageShownForStocks.insert(stockSymbol)
    }

    // MARK: - Refresh Logic

    private func isNewDay(since lastDate: Date?) -> Bool {
        guard let lastDate = lastDate else { return true }

        let calendar = Calendar.current
        let lastDay = calendar.startOfDay(for: lastDate)
        let today = calendar.startOfDay(for: Date())

        return today > lastDay
    }

    private func isNewTradingDay(since lastDate: Date?) -> Bool {
        guard let lastDate = lastDate else { return true }

        let calendar = Calendar.current
        let eastern = TimeZone(identifier: "America/New_York")!
        var calendarET = Calendar(identifier: .gregorian)
        calendarET.timeZone = eastern

        let lastDay = calendarET.startOfDay(for: lastDate)
        let today = calendarET.startOfDay(for: Date())

        return today > lastDay
    }

    func checkAndRefreshIfNeeded() {
        // Check if we need to refresh prices (new trading day)
        if isNewTradingDay(since: lastPriceFetchDate) {
            refreshPrices()
        }

        // Check if we need to refresh news for any stocks (new day)
        for symbol in watchlistStocks {
            if isNewDay(since: lastNewsFetchDate[symbol]) {
                refreshNewsForStock(symbol)
            }
        }
    }

    private func refreshNewsForStock(_ symbol: String) {
        Task {
            do {
                let allNews = try await newsService.fetchNews(symbol: symbol)

                // Get swiped article URLs to exclude them from new articles
                let swipedArticleIds = swipedArticles[symbol] ?? []
                let existingArticles = newsDataMap[symbol] ?? []

                // Create a set of URLs/titles from swiped articles for matching
                let swipedURLs = Set(existingArticles
                    .filter { swipedArticleIds.contains($0.id) }
                    .compactMap { $0.url })

                let swipedTitles = Set(existingArticles
                    .filter { swipedArticleIds.contains($0.id) }
                    .map { $0.title })

                // Filter out articles that match swiped ones (by URL or title)
                let filteredNews = allNews.filter { article in
                    // Exclude if URL matches a swiped article
                    if let url = article.url, swipedURLs.contains(url) {
                        return false
                    }
                    // Exclude if title matches a swiped article
                    if swipedTitles.contains(article.title) {
                        return false
                    }
                    return true
                }

                if !filteredNews.isEmpty {
                    // Replace news articles on news page with filtered list
                    newsDataMap[symbol] = filteredNews
                    lastNewsFetchDate[symbol] = Date()

                    // Reset article index and reopen stock since we have new articles
                    articleIndexPerStock[symbol] = 0
                    endMessageShownForStocks.remove(symbol)
                    closedStocks.remove(symbol)  // Reopen the stock

                    // Note: swipedArticles[symbol] is NOT cleared - those persist until stock is removed
                }
            } catch {
                // Silent refresh - don't show errors
                print("Failed to refresh news for \(symbol): \(error)")
            }
        }
    }
}
