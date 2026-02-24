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
                Spacer()
                Text("Your watchlist is empty.\nAdd stocks to get started.")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.gray)
                Spacer()
            } else {
                // Stock symbol header
                Text(stockSymbol)
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
                        .foregroundStyle(currentStockIndex < viewModel.watchlistStocks.count - 1 ? .white : Color(white: 0.3))
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
                    let currentArticle = articles[currentArticleIndex]

                    VStack(alignment: .leading, spacing: 0) {
                        Text(currentArticle.title)
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundStyle(.white)
                            .lineSpacing(4)

                        Spacer().frame(height: 10)

                        Text("\(currentArticle.publishedAt) \u{00B7} \(currentArticle.publisher)")
                            .font(.caption)
                            .foregroundStyle(.gray)

                        Spacer().frame(height: 16)

                        ScrollView {
                            Text(currentArticle.summary)
                                .font(.body)
                                .foregroundStyle(Color(white: 0.85))
                                .lineSpacing(4)
                        }
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color(white: 0.11))
                    )
                    .padding(.horizontal, 16)
                    .offset(x: dragOffset.width)
                    .opacity(1.0 - min(Double(abs(dragOffset.width)) / 300.0, 0.5))
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
        .background(Color.black)
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
                        if vertical > 0 {
                            if currentStockIndex > 0 {
                                currentStockIndex -= 1
                                viewModel.selectedStock = viewModel.watchlistStocks[currentStockIndex]
                            }
                        } else {
                            if currentStockIndex < viewModel.watchlistStocks.count - 1 {
                                currentStockIndex += 1
                                viewModel.selectedStock = viewModel.watchlistStocks[currentStockIndex]
                            }
                        }
                    } else if abs(horizontal) > abs(vertical) && abs(horizontal) > 100 {
                        if !showEndMessage {
                            if horizontal > 0 {
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
