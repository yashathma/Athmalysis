import SwiftUI

struct SearchView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var searchQuery = ""

    private let allStocks: [Stock] = [
        Stock(symbol: "NVDA", name: "NVIDIA Corporation", currentPrice: 875.43, priceChange: 25.67, percentageChange: 3.02),
        Stock(symbol: "TSM", name: "Taiwan Semiconductor", currentPrice: 145.22, priceChange: -2.45, percentageChange: -1.66),
        Stock(symbol: "QQQ", name: "Invesco QQQ Trust", currentPrice: 425.18, priceChange: -5.12, percentageChange: -1.19),
        Stock(symbol: "AAPL", name: "Apple Inc.", currentPrice: 189.95, priceChange: 3.44, percentageChange: 1.84),
        Stock(symbol: "MSFT", name: "Microsoft Corporation", currentPrice: 415.26, priceChange: 8.12, percentageChange: 2.00),
        Stock(symbol: "GOOGL", name: "Alphabet Inc.", currentPrice: 2847.15, priceChange: -15.23, percentageChange: -0.53),
        Stock(symbol: "AMZN", name: "Amazon.com Inc.", currentPrice: 3467.42, priceChange: 22.18, percentageChange: 0.64),
        Stock(symbol: "META", name: "Meta Platforms Inc.", currentPrice: 485.12, priceChange: -7.33, percentageChange: -1.49),
        Stock(symbol: "TSLA", name: "Tesla Inc.", currentPrice: 248.87, priceChange: 12.45, percentageChange: 5.26),
        Stock(symbol: "NFLX", name: "Netflix Inc.", currentPrice: 598.34, priceChange: -3.21, percentageChange: -0.53),
        Stock(symbol: "CRM", name: "Salesforce Inc.", currentPrice: 267.89, priceChange: 4.56, percentageChange: 1.73),
        Stock(symbol: "INTC", name: "Intel Corporation", currentPrice: 42.18, priceChange: -0.87, percentageChange: -2.02),
        Stock(symbol: "AMD", name: "Advanced Micro Devices", currentPrice: 152.44, priceChange: 6.23, percentageChange: 4.26)
    ]

    private var filteredStocks: [Stock] {
        let available = allStocks.filter { !viewModel.watchlistStocks.contains($0.symbol) }

        if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
            return available
        }

        return available.filter {
            $0.symbol.localizedCaseInsensitiveContains(searchQuery) ||
            $0.name.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Add to Watchlist")
                .font(.title)
                .fontWeight(.bold)
                .padding(.horizontal, 16)
                .padding(.top, 16)

            Spacer().frame(height: 16)

            // Search field
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.secondary)
                TextField("Search stocks", text: $searchQuery)
                    .textFieldStyle(.plain)
                    .autocorrectionDisabled()
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(Color.secondary.opacity(0.3), lineWidth: 1)
            )
            .padding(.horizontal, 16)

            Spacer().frame(height: 16)

            if filteredStocks.isEmpty {
                Spacer()
                HStack {
                    Spacer()
                    Text("No stocks found")
                        .foregroundStyle(.secondary)
                    Spacer()
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(filteredStocks) { stock in
                            SearchResultCard(stock: stock) {
                                viewModel.addStock(stock.symbol)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct SearchResultCard: View {
    let stock: Stock
    let onAdd: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(stock.symbol)
                    .font(.headline)
                    .fontWeight(.bold)
                Text(stock.name)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button(action: onAdd) {
                Image(systemName: "plus")
                    .foregroundStyle(.blue)
                    .frame(width: 44, height: 44)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.08), radius: 2, y: 1)
        )
    }
}
