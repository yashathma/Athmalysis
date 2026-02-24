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
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)

                Spacer()

                Button(action: {
                    viewModel.watchlistNavPath.append(WatchlistRoute.search)
                }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundStyle(.gray)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)

            Spacer().frame(height: 16)

            if displayStocks.isEmpty {
                Spacer()
                HStack {
                    Spacer()
                    Text("Your watchlist is empty.\nTap + to add stocks.")
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.gray)
                    Spacer()
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(displayStocks) { stock in
                            StockRow(stock: stock) {
                                viewModel.selectedStock = stock.symbol
                                selectedTab = 1
                            } onRemove: {
                                viewModel.removeStock(stock.symbol)
                            }

                            if stock.id != displayStocks.last?.id {
                                Divider()
                                    .background(Color(white: 0.2))
                                    .padding(.leading, 60)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .background(Color.black)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.startAutoRefresh()
        }
        .onDisappear {
            viewModel.stopAutoRefresh()
        }
    }
}

struct StockRow: View {
    let stock: Stock
    let onClick: () -> Void
    let onRemove: () -> Void

    @State private var showDelete = false

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 3) {
                    Text(stock.symbol)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundStyle(.white)
                    Text(stock.name)
                        .font(.caption)
                        .foregroundStyle(.gray)
                        .lineLimit(1)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 3) {
                    Text(String(format: "$%.2f", stock.currentPrice))
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.white)

                    Text("\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.priceChange)) (\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.percentageChange))%)")
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(
                            RoundedRectangle(cornerRadius: 5)
                                .fill(stock.isPositive ? Color.green : Color.red)
                        )
                        .foregroundStyle(.white)
                }
            }
            .padding(.vertical, 12)
        }
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button(role: .destructive, action: onRemove) {
                Label("Delete", systemImage: "trash")
            }
        }
        .contextMenu {
            Button(role: .destructive, action: onRemove) {
                Label("Remove from Watchlist", systemImage: "trash")
            }
        }
    }
}
