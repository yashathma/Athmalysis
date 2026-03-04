import SwiftUI

struct AISummaryView: View {
    @EnvironmentObject var viewModel: AppViewModel

    private var stocksWithSwipedArticles: [(symbol: String, count: Int)] {
        viewModel.swipedArticles
            .filter { !$0.value.isEmpty }
            .map { (symbol: $0.key, count: $0.value.count) }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                // Header
                Text("AI Summaries")
                    .font(.title)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)

                Text("Stocks you've swiped right on")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                if stocksWithSwipedArticles.isEmpty {
                    Spacer().frame(height: 32)
                    Text("No articles swiped yet.\nSwipe right on articles in the News screen!")
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.secondary)
                        .padding(32)
                } else {
                    ForEach(stocksWithSwipedArticles, id: \.symbol) { item in
                        StockSummaryCard(
                            stockSymbol: item.symbol,
                            articleCount: item.count
                        ) {
                            viewModel.aiNavPath.append(AIRoute.detailedAISummary(item.symbol))
                        }
                    }
                }
            }
            .padding(16)
        }
        .navigationBarHidden(true)
    }
}

struct StockSummaryCard: View {
    let stockSymbol: String
    let articleCount: Int
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 10) {
                Text(stockSymbol)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)

                Text("You swiped right on \(articleCount) article\(articleCount != 1 ? "s" : "")")
                    .font(.body)
                    .foregroundStyle(.gray)
                    .lineSpacing(4)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(20)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(white: 0.11))
            )
        }
    }
}
