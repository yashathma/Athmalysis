import Foundation

class YahooFinanceService {
    static let shared = YahooFinanceService()
    private let session: URLSession

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 30
        config.httpAdditionalHeaders = [
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
        ]
        session = URLSession(configuration: config)
    }

    // MARK: - Fetch Stock Data

    func fetchStockData(symbol: String, name: String? = nil) async throws -> Stock {
        var components = URLComponents(string: "https://query1.finance.yahoo.com/v8/finance/chart/\(symbol)")!
        components.queryItems = [
            URLQueryItem(name: "interval", value: "1d"),
            URLQueryItem(name: "range", value: "1d")
        ]

        guard let url = components.url else {
            print("[YF] ERROR: Could not build URL for \(symbol)")
            throw ServiceError.noData(symbol)
        }

        print("[YF] Fetching: \(url.absoluteString)")

        do {
            let (data, response) = try await session.data(from: url)

            if let httpResponse = response as? HTTPURLResponse {
                print("[YF] Status: \(httpResponse.statusCode)")
            }

            if let raw = String(data: data.prefix(500), encoding: .utf8) {
                print("[YF] Response: \(raw)")
            }

            return try parseChartResponse(data: data, symbol: symbol, name: name)
        } catch {
            print("[YF] Error fetching \(symbol): \(error)")
            throw error
        }
    }

    private func parseChartResponse(data: Data, symbol: String, name: String?) throws -> Stock {
        // Debug: check if we got HTML instead of JSON
        if let raw = String(data: data, encoding: .utf8), raw.hasPrefix("<") {
            throw ServiceError.apiError(403)
        }

        let response: YahooChartResponse
        do {
            response = try JSONDecoder().decode(YahooChartResponse.self, from: data)
        } catch {
            throw ServiceError.noData(symbol)
        }

        if let err = response.chart?.error {
            throw ServiceError.noData("\(symbol): \(err.description ?? "unknown")")
        }

        guard let meta = response.chart?.result?.first?.meta,
              let price = meta.regularMarketPrice else {
            throw ServiceError.noData(symbol)
        }

        let previousClose = meta.previousClose ?? meta.chartPreviousClose ?? price
        let change = price - previousClose
        let percent = previousClose > 0 ? (change / previousClose) * 100 : 0
        let stockName = name ?? meta.shortName ?? meta.longName ?? symbol

        return Stock(
            symbol: meta.symbol ?? symbol,
            name: stockName,
            currentPrice: price,
            priceChange: change,
            percentageChange: percent
        )
    }

    // MARK: - Refresh Multiple Stock Prices

    func refreshStockPrices(symbols: [String], stockDataMap: [String: Stock]) async throws -> [Stock] {
        var stocks: [Stock] = []
        for symbol in symbols {
            let existingName = stockDataMap[symbol]?.name
            let stock = try await fetchStockData(symbol: symbol, name: existingName)
            stocks.append(stock)
        }
        return stocks
    }

    // MARK: - Search Stocks

    struct SearchResult: Identifiable {
        let symbol: String
        let name: String
        var id: String { symbol }
    }

    func searchStocks(query: String) async throws -> [SearchResult] {
        var components = URLComponents(string: "https://query1.finance.yahoo.com/v1/finance/search")!
        components.queryItems = [
            URLQueryItem(name: "q", value: query),
            URLQueryItem(name: "quotesCount", value: "15"),
            URLQueryItem(name: "newsCount", value: "0")
        ]

        guard let url = components.url else { return [] }

        let (data, _) = try await session.data(from: url)
        let response = try JSONDecoder().decode(YahooSearchResponse.self, from: data)

        let quotes = response.quotes ?? []
        return quotes.compactMap { quote in
            guard let symbol = quote.symbol,
                  let type = quote.quoteType,
                  type == "EQUITY" || type == "ETF" else {
                return nil
            }
            let name = quote.longname ?? quote.shortname ?? symbol
            return SearchResult(symbol: symbol, name: name)
        }
    }
}
