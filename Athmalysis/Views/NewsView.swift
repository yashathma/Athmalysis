import SwiftUI

struct NewsView: View {
    @EnvironmentObject var viewModel: AppViewModel

    @State private var currentStockIndex: Int = 0
    @State private var dragOffset: CGSize = .zero
    @State private var verticalDragOffset: CGFloat = 0

    // Filter out closed stocks for smooth scrolling
    private var visibleStocks: [String] {
        viewModel.watchlistStocks.filter { !viewModel.closedStocks.contains($0) }
    }

    private var stockSymbol: String {
        viewModel.selectedStock
    }

    private var articles: [NewsArticle] {
        viewModel.newsDataMap[stockSymbol] ?? []
    }

    private var currentArticleIndex: Int {
        viewModel.articleIndexPerStock[stockSymbol] ?? 0
    }

    private var showEndMessage: Bool {
        viewModel.endMessageShownForStocks.contains(stockSymbol)
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Show current stock and adjacent stocks for smooth transitions
                ForEach(Array(visibleStocks.enumerated()), id: \.element) { index, symbol in
                    if abs(index - currentStockIndex) <= 1 {
                        stockContentView(for: symbol, at: index, screenHeight: geometry.size.height)
                    }
                }
            }
        }
        .background(Color.black)
        .contentShape(Rectangle())
        .gesture(
            DragGesture()
                .onChanged { value in
                    let horizontal = value.translation.width
                    let vertical = value.translation.height

                    if abs(vertical) > abs(horizontal) {
                        // Vertical drag for stock switching
                        verticalDragOffset = vertical
                        dragOffset = .zero
                    } else {
                        // Horizontal drag for article swiping
                        dragOffset = CGSize(width: horizontal, height: 0)
                        verticalDragOffset = 0
                    }
                }
                .onEnded { value in
                    let horizontal = value.translation.width
                    let vertical = value.translation.height

                    if abs(vertical) > abs(horizontal) && abs(vertical) > 100 {
                        // Stock switching (only visible stocks, smooth!)
                        withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                            if vertical > 0 {
                                if currentStockIndex > 0 {
                                    currentStockIndex -= 1
                                    viewModel.selectedStock = visibleStocks[currentStockIndex]
                                }
                            } else {
                                if currentStockIndex < visibleStocks.count - 1 {
                                    currentStockIndex += 1
                                    viewModel.selectedStock = visibleStocks[currentStockIndex]
                                }
                            }
                            verticalDragOffset = 0
                        }
                    } else if abs(horizontal) > abs(vertical) && abs(horizontal) > 100 {
                        // Article swiping - throw card off screen
                        if !showEndMessage {
                            let throwTarget = horizontal > 0 ? UIScreen.main.bounds.width * 1.5 : -UIScreen.main.bounds.width * 1.5

                            // Animate card flying off screen
                            withAnimation(.easeIn(duration: 0.3)) {
                                dragOffset = CGSize(width: throwTarget, height: 0)
                            }

                            // After animation completes, update state and reset
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                if horizontal > 0 {
                                    if !articles.isEmpty && currentArticleIndex < articles.count {
                                        let currentArticle = articles[currentArticleIndex]
                                        viewModel.swipeArticle(stockSymbol: stockSymbol, articleId: currentArticle.id)

                                        if currentArticleIndex == articles.count - 1 {
                                            // Last article swiped - close stock and move to next
                                            viewModel.markEndMessageShown(stockSymbol: stockSymbol)
                                            closeStockAndMoveToNext()
                                        } else {
                                            viewModel.setArticleIndex(stockSymbol: stockSymbol, index: currentArticleIndex + 1)
                                        }
                                    }
                                } else {
                                    if !articles.isEmpty {
                                        if currentArticleIndex == articles.count - 1 {
                                            // Last article skipped - close stock and move to next
                                            closeStockAndMoveToNext()
                                        } else {
                                            viewModel.setArticleIndex(stockSymbol: stockSymbol, index: currentArticleIndex + 1)
                                        }
                                    }
                                }

                                // Reset offset instantly (no animation) so next card appears clean
                                dragOffset = .zero
                            }
                            verticalDragOffset = 0
                            return
                        }
                    }

                    withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                        dragOffset = .zero
                        verticalDragOffset = 0
                    }
                }
        )
        .navigationBarHidden(true)
        .onAppear {
            // Use visible stocks only
            if let idx = visibleStocks.firstIndex(of: stockSymbol) {
                currentStockIndex = idx
            } else if !visibleStocks.isEmpty {
                // If selected stock is closed or not found, go to first visible stock
                currentStockIndex = 0
                viewModel.selectedStock = visibleStocks[0]
            }
        }
        .onChange(of: viewModel.selectedStock) { _, newValue in
            if let idx = visibleStocks.firstIndex(of: newValue) {
                currentStockIndex = idx
            }
        }
    }

    private func closeStockAndMoveToNext() {
        let closingIndex = currentStockIndex
        let closingSymbol = stockSymbol
        let hasStockBelow = closingIndex < visibleStocks.count - 1

        // Check what the list looks like after closing
        let updatedStocks = viewModel.watchlistStocks.filter {
            !viewModel.closedStocks.contains($0) && $0 != closingSymbol
        }

        if updatedStocks.isEmpty {
            viewModel.closedStocks.insert(closingSymbol)
            return
        }

        if hasStockBelow {
            // Stock below bounces UP: animate scroll down first, then remove
            let nextSymbol = visibleStocks[closingIndex + 1]

            withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                currentStockIndex = closingIndex + 1
                viewModel.selectedStock = nextSymbol
            }

            // After animation finishes, close the stock and fix the index
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                viewModel.closedStocks.insert(closingSymbol)
                // Fix index: the list shifted, recalculate position
                if let newIdx = self.visibleStocks.firstIndex(of: nextSymbol) {
                    self.currentStockIndex = newIdx
                }
            }
        } else {
            // Last stock: stock above bounces DOWN
            let targetIndex = min(closingIndex, updatedStocks.count - 1)
            viewModel.closedStocks.insert(closingSymbol)
            withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                currentStockIndex = targetIndex
                viewModel.selectedStock = updatedStocks[targetIndex]
            }
        }
    }

    @ViewBuilder
    private func articleCard(for article: NewsArticle, isNext: Bool) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            if let urlString = article.url, let url = URL(string: urlString) {
                Text(article.title)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundStyle(.blue)
                    .lineSpacing(4)
                    .underline()
                    .onTapGesture {
                        if !isNext {
                            UIApplication.shared.open(url)
                        }
                    }
            } else {
                Text(article.title)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .lineSpacing(4)
            }

            Spacer().frame(height: 10)

            Text("\(article.timeAgo) \u{00B7} \(article.publisher)")
                .font(.caption)
                .foregroundStyle(.gray)

            Spacer().frame(height: 16)

            Text(article.summary)
                .font(.body)
                .foregroundStyle(Color(white: 0.85))
                .lineSpacing(4)
                .minimumScaleFactor(0.5)
        }
        .padding(20)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(white: 0.11))
        )
        .padding(.horizontal, 16)
    }

    @ViewBuilder
    private func stockContentView(for symbol: String, at index: Int, screenHeight: CGFloat) -> some View {
        let pageSpacing = screenHeight + 100 // Extra gap so next stock title is below tab bar
        let offset = CGFloat(index - currentStockIndex) * pageSpacing + verticalDragOffset
        let articles = viewModel.newsDataMap[symbol] ?? []
        let currentArticleIndex = viewModel.articleIndexPerStock[symbol] ?? 0
        let showEndMessage = viewModel.endMessageShownForStocks.contains(symbol)

        VStack(spacing: 0) {
            if visibleStocks.isEmpty || symbol.isEmpty {
                Spacer()
                Text("Your watchlist is empty.\nAdd stocks to get started.")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.gray)
                Spacer()
            } else {
                // Stock name header
                Text(viewModel.stockDataMap[symbol]?.name ?? symbol)
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 16)

                Spacer().frame(height: 8)

                // Stock navigation indicators
                HStack(spacing: 8) {
                    Image(systemName: "chevron.up")
                        .font(.caption2)
                        .foregroundStyle(currentStockIndex > 0 ? .white : Color(white: 0.3))
                    Text("Swipe to change stocks")
                        .font(.caption)
                        .foregroundStyle(.gray)
                    Image(systemName: "chevron.down")
                        .font(.caption2)
                        .foregroundStyle(currentStockIndex < visibleStocks.count - 1 ? .white : Color(white: 0.3))
                }

                Spacer().frame(height: 16)

                // Article display or AI Summary button
                if showEndMessage {
                    Spacer()
                    Button(action: {
                        viewModel.newsNavPath.append(NewsRoute.detailedAISummary(symbol))
                    }) {
                        HStack(spacing: 12) {
                            Image(systemName: "sparkles")
                                .font(.title3)
                            Text("Generate AI Summary")
                                .font(.title3)
                                .fontWeight(.semibold)
                        }
                        .foregroundStyle(.black)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(Color.green)
                        )
                    }
                    Spacer()
                } else if !articles.isEmpty && currentArticleIndex < articles.count {
                    // Show stacked cards: current and next article
                    ZStack {
                        // Next article card (behind, full size, just sitting there)
                        if currentArticleIndex + 1 < articles.count {
                            articleCard(for: articles[currentArticleIndex + 1], isNext: true)
                        }

                        // Current article card (on top)
                        articleCard(for: articles[currentArticleIndex], isNext: false)
                            .offset(x: index == currentStockIndex ? dragOffset.width : 0)
                            .rotationEffect(.degrees(index == currentStockIndex ? Double(dragOffset.width) / 20 : 0))
                            .opacity(index == currentStockIndex ? 1.0 - min(Double(abs(dragOffset.width)) / 400.0, 0.3) : 1.0)
                            .zIndex(1)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    Spacer()
                    Text("No articles available")
                        .foregroundStyle(.gray)
                    Spacer()
                }

                Spacer().frame(height: 16)

                // Gesture instructions
                HStack(spacing: 32) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.left")
                            .font(.caption2)
                        Text("Skip")
                            .font(.caption)
                    }
                    .foregroundStyle(.red)

                    HStack(spacing: 4) {
                        Text("Include")
                            .font(.caption)
                        Image(systemName: "arrow.right")
                            .font(.caption2)
                    }
                    .foregroundStyle(.green)
                }

                // Article counter
                if !articles.isEmpty && !showEndMessage {
                    Text("\(currentArticleIndex + 1) of \(articles.count)")
                        .font(.caption)
                        .foregroundStyle(.gray)
                        .padding(.top, 4)
                }

                Spacer().frame(height: 8)
            }
        }
        .frame(width: UIScreen.main.bounds.width, height: screenHeight)
        .background(Color.black)
        .offset(y: offset)
    }
}
