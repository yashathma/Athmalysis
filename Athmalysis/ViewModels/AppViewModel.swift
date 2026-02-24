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
    @Published var articleIndexPerStock: [String: Int] {
        didSet { DataManager.shared.saveArticleIndex(articleIndexPerStock) }
    }
    @Published var endMessageShownForStocks: Set<String> {
        didSet { DataManager.shared.saveEndMessageShown(endMessageShownForStocks) }
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
        self.articleIndexPerStock = dm.loadArticleIndex()
        self.endMessageShownForStocks = dm.loadEndMessageShown()
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
        articleIndexPerStock.removeValue(forKey: symbol)
        endMessageShownForStocks.remove(symbol)
        stockDataMap.removeValue(forKey: symbol)
        newsDataMap.removeValue(forKey: symbol)
        DataManager.shared.clearStockData(symbol)

        if selectedStock == symbol {
            selectedStock = watchlistStocks.first ?? ""
        }
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
            } catch {
                // Silent refresh — don't show errors to user
            }
        }
    }

    func swipeArticle(stockSymbol: String, articleId: String) {
        var current = swipedArticles[stockSymbol] ?? []
        current.insert(articleId)
        swipedArticles[stockSymbol] = current
    }

    func setArticleIndex(stockSymbol: String, index: Int) {
        articleIndexPerStock[stockSymbol] = index
    }

    func markEndMessageShown(stockSymbol: String) {
        endMessageShownForStocks.insert(stockSymbol)
    }
}
