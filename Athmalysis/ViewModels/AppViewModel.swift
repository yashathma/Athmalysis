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

    let maxWatchlistSize = 3
    private let service = AlphaVantageService.shared

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

    func addStock(_ symbol: String) {
        guard !watchlistStocks.contains(symbol),
              watchlistStocks.count < maxWatchlistSize else { return }

        isLoadingStock = true
        errorMessage = nil

        Task {
            do {
                let (stock, news) = try await service.fetchStockAndNews(symbol: symbol)
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
                let stocks = try await service.refreshStockPrices(symbols: watchlistStocks)
                for stock in stocks {
                    stockDataMap[stock.symbol] = stock
                }
            } catch {
                let msg = error.localizedDescription
                if msg.contains("rate limit") {
                    errorMessage = "API key limit reached. Please try again later."
                } else {
                    errorMessage = "Failed to refresh: \(msg)"
                }
            }
            isRefreshing = false
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
