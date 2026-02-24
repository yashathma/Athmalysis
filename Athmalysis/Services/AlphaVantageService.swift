import Foundation

class AlphaVantageService {
    static let shared = AlphaVantageService()
    private let baseURL = "https://www.alphavantage.co/query"
    private let session: URLSession

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        session = URLSession(configuration: config)
    }

    // MARK: - Fetch News

    func fetchNews(symbol: String, attemptCount: Int = 0) async throws -> [NewsArticle] {
        if attemptCount >= ApiKeyManager.shared.totalKeys {
            throw ServiceError.rateLimitExhausted
        }

        let apiKey = ApiKeyManager.shared.getNextKey()
        var components = URLComponents(string: baseURL)!
        components.queryItems = [
            URLQueryItem(name: "function", value: "NEWS_SENTIMENT"),
            URLQueryItem(name: "tickers", value: symbol),
            URLQueryItem(name: "apikey", value: apiKey)
        ]

        let (data, _) = try await session.data(from: components.url!)
        let response = try JSONDecoder().decode(NewsSentimentResponse.self, from: data)

        // Check rate limit
        if response.information != nil || response.note != nil {
            return try await fetchNews(symbol: symbol, attemptCount: attemptCount + 1)
        }

        let newsItems = Array((response.feed ?? []).prefix(20))
        return newsItems.enumerated().map { index, item in
            NewsArticle(
                id: "\(symbol)_\(index + 1)",
                title: item.title ?? "Article \(index + 1)",
                summary: item.summary ?? "No summary available",
                publishedAt: Self.formatTimeAgo(item.timePublished),
                publisher: item.source ?? "Unknown Publisher",
                stockSymbol: symbol
            )
        }
    }

    // MARK: - Helpers

    static func formatTimeAgo(_ timestamp: String?) -> String {
        guard let timestamp = timestamp else { return "Unknown time" }

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMdd'T'HHmmss"
        formatter.timeZone = TimeZone(identifier: "UTC")

        guard let date = formatter.date(from: timestamp) else { return timestamp }

        let now = Date()
        let diff = now.timeIntervalSince(date)

        let seconds = Int(diff)
        let minutes = seconds / 60
        let hours = minutes / 60
        let days = hours / 24

        if days > 0 {
            return "\(days) day\(days > 1 ? "s" : "") ago"
        } else if hours > 0 {
            return "\(hours) hour\(hours > 1 ? "s" : "") ago"
        } else if minutes > 0 {
            return "\(minutes) minute\(minutes > 1 ? "s" : "") ago"
        } else {
            return "Just now"
        }
    }
}

// MARK: - Service Errors

enum ServiceError: LocalizedError {
    case rateLimitExhausted
    case noData(String)
    case noNews(String)
    case apiError(Int)

    var errorDescription: String? {
        switch self {
        case .rateLimitExhausted:
            return "All API keys have reached their rate limit"
        case .noData(let symbol):
            return "No data returned for \(symbol)"
        case .noNews(let symbol):
            return "No news available for \(symbol)"
        case .apiError(let code):
            return "API error: \(code)"
        }
    }
}
