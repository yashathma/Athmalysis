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

// MARK: - Yahoo Finance API Response Models

struct YahooChartResponse: Codable {
    let chart: YahooChart?
}

struct YahooChart: Codable {
    let result: [YahooChartResult]?
    let error: YahooError?
}

struct YahooChartResult: Codable {
    let meta: YahooMeta?
}

struct YahooMeta: Codable {
    let symbol: String?
    let regularMarketPrice: Double?
    let previousClose: Double?
    let chartPreviousClose: Double?
    let regularMarketDayHigh: Double?
    let regularMarketDayLow: Double?
    let shortName: String?
    let longName: String?
}

struct YahooError: Codable {
    let code: String?
    let description: String?
}

struct YahooSearchResponse: Codable {
    let quotes: [YahooQuote]?
}

struct YahooQuote: Codable {
    let symbol: String?
    let shortname: String?
    let longname: String?
    let quoteType: String?
    let exchange: String?
}

// MARK: - Alpha Vantage API Response Models (News only)

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
