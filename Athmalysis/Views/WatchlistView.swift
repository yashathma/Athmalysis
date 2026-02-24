import SwiftUI

struct WatchlistView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Binding var selectedTab: Int

    private var displayStocks: [Stock] {
        viewModel.watchlistStocks.compactMap { viewModel.stockDataMap[$0] }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                Text("Watchlist")
                    .font(.title)
                    .fontWeight(.bold)

                Spacer()

                Button(action: {
                    viewModel.watchlistNavPath.append(WatchlistRoute.search)
                }) {
                    Image(systemName: "magnifyingglass")
                        .font(.title3)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)

            Spacer().frame(height: 16)

            if displayStocks.isEmpty {
                Spacer()
                HStack {
                    Spacer()
                    Text("Your watchlist is empty.\nTap the search icon to add stocks!")
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.secondary)
                    Spacer()
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(displayStocks) { stock in
                            StockCard(stock: stock) {
                                viewModel.selectedStock = stock.symbol
                                selectedTab = 1
                            } onRemove: {
                                viewModel.removeStock(stock.symbol)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.startAutoRefresh()
        }
        .onDisappear {
            viewModel.stopAutoRefresh()
        }
    }
}

struct StockCard: View {
    let stock: Stock
    let onClick: () -> Void
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            // Delete button
            Button(action: onRemove) {
                Image(systemName: "trash")
                    .foregroundStyle(.red)
                    .frame(width: 44, height: 44)
            }

            // Stock info (tappable)
            Button(action: onClick) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(stock.symbol)
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundStyle(.primary)
                        Text(stock.name)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 4) {
                        Text(String(format: "$%.2f", stock.currentPrice))
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundStyle(.primary)

                        HStack(spacing: 4) {
                            Image(systemName: stock.isPositive ? "arrow.up.right" : "arrow.down.right")
                                .font(.caption2)

                            Text("\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.priceChange)) (\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.percentageChange))%)")
                                .font(.caption)
                                .fontWeight(.medium)
                        }
                        .foregroundStyle(stock.isPositive ? Color(red: 0.3, green: 0.69, blue: 0.31) : Color(red: 0.96, green: 0.26, blue: 0.21))
                    }
                }
                .padding(.horizontal, 8)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
        )
    }
}
