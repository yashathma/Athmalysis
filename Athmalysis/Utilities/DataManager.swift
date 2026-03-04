import Foundation

class DataManager {
    static let shared = DataManager()
    private let defaults = UserDefaults.standard
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    private enum Keys {
        static let watchlist = "watchlist_stocks"
        static let stockData = "stock_data_map"
        static let newsData = "news_data_map"
        static let swipedArticles = "swiped_articles"
        static let articleIndex = "article_index_per_stock"
        static let endMessageShown = "end_message_shown_stocks"
        static let selectedStock = "selected_stock"
        static let lastNewsFetchDate = "last_news_fetch_date"
        static let lastPriceFetchDate = "last_price_fetch_date"
    }

    private init() {}

    // MARK: - Watchlist

    func saveWatchlist(_ stocks: [String]) {
        if let data = try? encoder.encode(stocks) {
            defaults.set(data, forKey: Keys.watchlist)
        }
    }

    func loadWatchlist() -> [String] {
        guard let data = defaults.data(forKey: Keys.watchlist),
              let stocks = try? decoder.decode([String].self, from: data) else {
            return []
        }
        return stocks
    }

    // MARK: - Stock Data

    func saveStockData(_ stockDataMap: [String: Stock]) {
        if let data = try? encoder.encode(stockDataMap) {
            defaults.set(data, forKey: Keys.stockData)
        }
    }

    func loadStockData() -> [String: Stock] {
        guard let data = defaults.data(forKey: Keys.stockData),
              let map = try? decoder.decode([String: Stock].self, from: data) else {
            return [:]
        }
        return map
    }

    // MARK: - News Data

    func saveNewsData(_ newsDataMap: [String: [NewsArticle]]) {
        if let data = try? encoder.encode(newsDataMap) {
            defaults.set(data, forKey: Keys.newsData)
        }
    }

    func loadNewsData() -> [String: [NewsArticle]] {
        guard let data = defaults.data(forKey: Keys.newsData),
              let map = try? decoder.decode([String: [NewsArticle]].self, from: data) else {
            return [:]
        }
        return map
    }

    // MARK: - Swiped Articles

    func saveSwipedArticles(_ swipedArticles: [String: Set<String>]) {
        // Convert Set to Array for encoding
        let arrayMap = swipedArticles.mapValues { Array($0) }
        if let data = try? encoder.encode(arrayMap) {
            defaults.set(data, forKey: Keys.swipedArticles)
        }
    }

    func loadSwipedArticles() -> [String: Set<String>] {
        guard let data = defaults.data(forKey: Keys.swipedArticles),
              let arrayMap = try? decoder.decode([String: [String]].self, from: data) else {
            return [:]
        }
        return arrayMap.mapValues { Set($0) }
    }

    // MARK: - Article Index

    func saveArticleIndex(_ articleIndexPerStock: [String: Int]) {
        if let data = try? encoder.encode(articleIndexPerStock) {
            defaults.set(data, forKey: Keys.articleIndex)
        }
    }

    func loadArticleIndex() -> [String: Int] {
        guard let data = defaults.data(forKey: Keys.articleIndex),
              let map = try? decoder.decode([String: Int].self, from: data) else {
            return [:]
        }
        return map
    }

    // MARK: - End Message Shown

    func saveEndMessageShown(_ stocks: Set<String>) {
        let array = Array(stocks)
        if let data = try? encoder.encode(array) {
            defaults.set(data, forKey: Keys.endMessageShown)
        }
    }

    func loadEndMessageShown() -> Set<String> {
        guard let data = defaults.data(forKey: Keys.endMessageShown),
              let array = try? decoder.decode([String].self, from: data) else {
            return []
        }
        return Set(array)
    }

    // MARK: - Selected Stock

    func saveSelectedStock(_ stock: String) {
        defaults.set(stock, forKey: Keys.selectedStock)
    }

    func loadSelectedStock() -> String {
        defaults.string(forKey: Keys.selectedStock) ?? ""
    }

    // MARK: - Last Fetch Dates

    func saveLastNewsFetchDate(_ dates: [String: Date]) {
        if let data = try? encoder.encode(dates) {
            defaults.set(data, forKey: Keys.lastNewsFetchDate)
        }
    }

    func loadLastNewsFetchDate() -> [String: Date] {
        guard let data = defaults.data(forKey: Keys.lastNewsFetchDate),
              let dates = try? decoder.decode([String: Date].self, from: data) else {
            return [:]
        }
        return dates
    }

    func saveLastPriceFetchDate(_ date: Date?) {
        if let date = date {
            defaults.set(date, forKey: Keys.lastPriceFetchDate)
        } else {
            defaults.removeObject(forKey: Keys.lastPriceFetchDate)
        }
    }

    func loadLastPriceFetchDate() -> Date? {
        return defaults.object(forKey: Keys.lastPriceFetchDate) as? Date
    }

    // MARK: - Clear

    func clearAll() {
        let domain = Bundle.main.bundleIdentifier!
        defaults.removePersistentDomain(forName: domain)
    }

    func clearStockData(_ symbol: String) {
        var stockData = loadStockData()
        var newsData = loadNewsData()
        var swiped = loadSwipedArticles()
        var index = loadArticleIndex()
        var endMsg = loadEndMessageShown()
        var newsFetchDate = loadLastNewsFetchDate()

        stockData.removeValue(forKey: symbol)
        newsData.removeValue(forKey: symbol)
        swiped.removeValue(forKey: symbol)
        index.removeValue(forKey: symbol)
        endMsg.remove(symbol)
        newsFetchDate.removeValue(forKey: symbol)

        saveStockData(stockData)
        saveNewsData(newsData)
        saveSwipedArticles(swiped)
        saveArticleIndex(index)
        saveEndMessageShown(endMsg)
        saveLastNewsFetchDate(newsFetchDate)
    }
}
