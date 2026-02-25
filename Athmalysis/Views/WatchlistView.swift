import SwiftUI

struct WatchlistView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Binding var selectedTab: Int
    @State private var activeSwipeID: String?

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
                            StockRow(stock: stock, activeSwipeID: $activeSwipeID) {
                                viewModel.selectedStock = stock.symbol
                                selectedTab = 1
                            } onRemove: {
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                                    viewModel.removeStock(stock.symbol)
                                }
                            }

                            if stock.id != displayStocks.last?.id {
                                Divider()
                                    .background(Color(white: 0.2))
                                    .padding(.horizontal, 4)
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
    @Binding var activeSwipeID: String?
    let onClick: () -> Void
    let onRemove: () -> Void

    @State private var offset: CGFloat = 0
    private let trashWidth: CGFloat = 70

    private var isRevealed: Bool {
        activeSwipeID == stock.symbol
    }

    var body: some View {
        ZStack(alignment: .trailing) {
            // Trash button behind the row
            if offset < 0 || isRevealed {
                Button {
                    onRemove()
                } label: {
                    ZStack {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 40, height: 40)
                        Image(systemName: "trash.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(.white)
                    }
                    .frame(width: trashWidth)
                    .frame(maxHeight: .infinity)
                }
                .padding(.vertical, 12)
                .transition(.move(edge: .trailing))
            }

            // Main row content
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
            .background(Color.black)
            .offset(x: offset)
            .gesture(
                DragGesture(minimumDistance: 20)
                    .onChanged { value in
                        let horizontal = value.translation.width
                        if isRevealed {
                            let newOffset = -trashWidth + horizontal
                            offset = min(0, max(-trashWidth, newOffset))
                        } else {
                            if horizontal < 0 {
                                // Close any other revealed row when this one starts swiping
                                if activeSwipeID != nil {
                                    activeSwipeID = nil
                                }
                                offset = max(-trashWidth, horizontal)
                            }
                        }
                    }
                    .onEnded { value in
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                            if isRevealed {
                                if value.translation.width > trashWidth * 0.3 {
                                    offset = 0
                                    activeSwipeID = nil
                                } else {
                                    offset = -trashWidth
                                }
                            } else {
                                if -value.translation.width > trashWidth * 0.4 {
                                    offset = -trashWidth
                                    activeSwipeID = stock.symbol
                                } else {
                                    offset = 0
                                }
                            }
                        }
                    }
            )
            .onTapGesture {
                if isRevealed {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                        offset = 0
                        activeSwipeID = nil
                    }
                } else {
                    onClick()
                }
            }
        }
        .clipped()
        .onChange(of: activeSwipeID) { _, newValue in
            // Another row became active — close this one
            if newValue != stock.symbol && offset != 0 {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    offset = 0
                }
            }
        }
    }
}
