import SwiftUI

struct DetailedAISummaryView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Environment(\.dismiss) var dismiss
    let stockSymbol: String

    private var articles: [NewsArticle] {
        viewModel.savedArticles[stockSymbol] ?? []
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
                            VStack(alignment: .leading, spacing: 0) {
                                // Title with link (matching NewsView style)
                                if let urlString = article.url, let url = URL(string: urlString) {
                                    Text(article.title)
                                        .font(.title3)
                                        .fontWeight(.bold)
                                        .foregroundStyle(.blue)
                                        .lineSpacing(4)
                                        .underline()
                                        .onTapGesture {
                                            UIApplication.shared.open(url)
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
                            }
                            .frame(maxWidth: .infinity, alignment: .topLeading)
                            .padding(20)
                            .background(
                                RoundedRectangle(cornerRadius: 16)
                                    .fill(Color(white: 0.11))
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
