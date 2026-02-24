import SwiftUI

struct NewsView: View {
    @EnvironmentObject var viewModel: AppViewModel

    @State private var currentStockIndex: Int = 0
    @State private var dragOffset: CGSize = .zero

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
        VStack(spacing: 0) {
            if viewModel.watchlistStocks.isEmpty || stockSymbol.isEmpty {
                // Empty state
                Spacer()
                Text("Your watchlist is empty.\nAdd stocks to get started!")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                // Stock symbol header
                Text(stockSymbol)
                    .font(.title)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 16)

                Spacer().frame(height: 16)

                // Stock navigation indicators
                HStack(spacing: 8) {
                    Image(systemName: "chevron.up")
                        .foregroundStyle(currentStockIndex > 0 ? Color.accentColor : .gray)
                    Text("Swipe up/down to change stocks")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Image(systemName: "chevron.down")
                        .foregroundStyle(currentStockIndex < viewModel.watchlistStocks.count - 1 ? Color.accentColor : .gray)
                }

                Spacer().frame(height: 16)

                // Article display or AI Summary button
                if showEndMessage {
                    Spacer()
                    Button(action: {
                        viewModel.newsNavPath.append(NewsRoute.detailedAISummary(stockSymbol))
                    }) {
                        HStack(spacing: 12) {
                            Image(systemName: "sparkles")
                                .font(.title3)
                            Text("Generate AI Summary")
                                .font(.title3)
                        }
                        .padding(.horizontal, 32)
                        .padding(.vertical, 16)
                    }
                    .buttonStyle(.borderedProminent)
                    Spacer()
                } else if !articles.isEmpty && currentArticleIndex < articles.count {
                    let currentArticle = articles[currentArticleIndex]

                    VStack(alignment: .leading, spacing: 0) {
                        Text(currentArticle.title)
                            .font(.title3)
                            .fontWeight(.bold)
                            .lineSpacing(4)

                        Spacer().frame(height: 12)

                        Text("\(currentArticle.publishedAt) by \(currentArticle.publisher)")
                            .font(.caption)
                            .foregroundStyle(.secondary)

                        Spacer().frame(height: 16)

                        ScrollView {
                            Text(currentArticle.summary)
                                .font(.body)
                                .lineSpacing(4)
                        }
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color(.systemBackground))
                            .shadow(color: .black.opacity(0.15), radius: 8, y: 4)
                    )
                    .padding(.horizontal, 16)
                    .offset(x: dragOffset.width)
                    .opacity(1.0 - min(Double(abs(dragOffset.width)) / 300.0, 0.5))
                } else {
                    Spacer()
                    Text("No articles available")
                        .foregroundStyle(.secondary)
                    Spacer()
                }

                Spacer().frame(height: 16)

                // Gesture instructions
                HStack(spacing: 32) {
                    HStack(spacing: 4) {
                        Image(systemName: "hand.point.left")
                            .font(.caption)
                        Text("Skip")
                            .font(.caption)
                    }
                    .foregroundStyle(Color(red: 0.96, green: 0.26, blue: 0.21))

                    HStack(spacing: 4) {
                        Image(systemName: "hand.point.right")
                            .font(.caption)
                        Text("Include")
                            .font(.caption)
                    }
                    .foregroundStyle(Color(red: 0.3, green: 0.69, blue: 0.31))
                }

                // Article counter
                if !articles.isEmpty && !showEndMessage {
                    Text("\(currentArticleIndex + 1) of \(articles.count)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .padding(.top, 4)
                }

                Spacer().frame(height: 8)
            }
        }
        .contentShape(Rectangle())
        .gesture(
            DragGesture()
                .onChanged { value in
                    dragOffset = value.translation
                }
                .onEnded { value in
                    let horizontal = value.translation.width
                    let vertical = value.translation.height

                    if abs(vertical) > abs(horizontal) && abs(vertical) > 100 {
                        // Vertical swipe - change stock
                        if vertical > 0 {
                            // Swipe down - previous stock
                            if currentStockIndex > 0 {
                                currentStockIndex -= 1
                                viewModel.selectedStock = viewModel.watchlistStocks[currentStockIndex]
                            }
                        } else {
                            // Swipe up - next stock
                            if currentStockIndex < viewModel.watchlistStocks.count - 1 {
                                currentStockIndex += 1
                                viewModel.selectedStock = viewModel.watchlistStocks[currentStockIndex]
                            }
                        }
                    } else if abs(horizontal) > abs(vertical) && abs(horizontal) > 100 {
                        if !showEndMessage {
                            if horizontal > 0 {
                                // Swipe right - include article
                                if !articles.isEmpty && currentArticleIndex < articles.count {
                                    let currentArticle = articles[currentArticleIndex]
                                    viewModel.swipeArticle(stockSymbol: stockSymbol, articleId: currentArticle.id)

                                    if currentArticleIndex == articles.count - 1 {
                                        viewModel.markEndMessageShown(stockSymbol: stockSymbol)
                                    } else {
                                        viewModel.setArticleIndex(stockSymbol: stockSymbol, index: currentArticleIndex + 1)
                                    }
                                }
                            } else {
                                // Swipe left - skip article
                                if !articles.isEmpty {
                                    if currentArticleIndex == articles.count - 1 {
                                        viewModel.markEndMessageShown(stockSymbol: stockSymbol)
                                    } else {
                                        viewModel.setArticleIndex(stockSymbol: stockSymbol, index: currentArticleIndex + 1)
                                    }
                                }
                            }
                        }
                    }

                    withAnimation(.spring(response: 0.3)) {
                        dragOffset = .zero
                    }
                }
        )
        .navigationBarHidden(true)
        .onAppear {
            if let idx = viewModel.watchlistStocks.firstIndex(of: stockSymbol) {
                currentStockIndex = idx
            }
        }
        .onChange(of: viewModel.selectedStock) { _, newValue in
            if let idx = viewModel.watchlistStocks.firstIndex(of: newValue) {
                currentStockIndex = idx
            }
        }
    }
}
