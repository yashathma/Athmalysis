import Foundation

struct Stock: Codable, Identifiable {
    let symbol: String
    let name: String
    let currentPrice: Double
    let priceChange: Double
    let percentageChange: Double

    var id: String { symbol }
    var isPositive: Bool { priceChange >= 0 }
}

struct NewsArticle: Codable, Identifiable {
    let id: String
    let title: String
    let summary: String
    let publishedAt: String
    let publisher: String
    let stockSymbol: String
}

struct AISummary: Codable {
    let stockSymbol: String
    let summary: String
    let keyPoints: [String]
    let sentiment: String
    let generatedAt: String
}

// MARK: - Alpha Vantage API Response Models

struct GlobalQuoteResponse: Codable {
    let globalQuote: GlobalQuote?
    let information: String?
    let note: String?

    enum CodingKeys: String, CodingKey {
        case globalQuote = "Global Quote"
        case information = "Information"
        case note = "Note"
    }
}

struct GlobalQuote: Codable {
    let symbol: String
    let price: String
    let change: String
    let changePercent: String

    enum CodingKeys: String, CodingKey {
        case symbol = "01. symbol"
        case price = "05. price"
        case change = "09. change"
        case changePercent = "10. change percent"
    }
}

struct NewsSentimentResponse: Codable {
    let feed: [NewsItem]?
    let information: String?
    let note: String?

    enum CodingKeys: String, CodingKey {
        case feed
        case information = "Information"
        case note = "Note"
    }
}

struct NewsItem: Codable {
    let title: String?
    let timePublished: String?
    let summary: String?
    let source: String?
    let url: String?

    enum CodingKeys: String, CodingKey {
        case title
        case timePublished = "time_published"
        case summary
        case source
        case url
    }
}
