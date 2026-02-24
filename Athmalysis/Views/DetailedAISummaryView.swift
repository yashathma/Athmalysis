import SwiftUI

struct DetailedAISummaryView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Environment(\.dismiss) var dismiss
    let stockSymbol: String

    private var swipedArticleIds: Set<String> {
        viewModel.swipedArticles[stockSymbol] ?? []
    }

    private var articles: [NewsArticle] {
        let allArticles = viewModel.newsDataMap[stockSymbol] ?? []
        return swipedArticleIds.compactMap { articleId in
            allArticles.first { $0.id == articleId }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header with back button
            HStack(spacing: 8) {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(.blue)
                }

                Text("Swiped Articles for \(stockSymbol)")
                    .font(.title2)
                    .fontWeight(.bold)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)

                Spacer()
            }
            .padding(16)

            if articles.isEmpty {
                Spacer()
                Text("No articles swiped for this stock")
                    .foregroundStyle(.secondary)
                    .padding(32)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(articles) { article in
                            VStack(alignment: .leading, spacing: 8) {
                                Text(article.title)
                                    .font(.title3)
                                    .fontWeight(.bold)

                                Text("\(article.publishedAt) by \(article.publisher)")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)

                                Spacer().frame(height: 4)

                                Text(article.summary)
                                    .font(.subheadline)
                                    .lineSpacing(2)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(16)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color(.systemBackground))
                                    .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .navigationBarHidden(true)
    }
}
