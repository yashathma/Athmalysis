import SwiftUI

struct SearchView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var searchQuery = ""
    @State private var searchResults: [YahooFinanceService.SearchResult] = []
    @State private var isSearching = false
    @State private var searchTask: Task<Void, Never>?

    private let suggestedStocks: [(symbol: String, name: String)] = [
        ("NVDA", "NVIDIA Corporation"),
        ("TSM", "Taiwan Semiconductor"),
        ("QQQ", "Invesco QQQ Trust"),
        ("AAPL", "Apple Inc."),
        ("MSFT", "Microsoft Corporation"),
        ("GOOGL", "Alphabet Inc."),
        ("AMZN", "Amazon.com Inc."),
        ("META", "Meta Platforms Inc."),
        ("TSLA", "Tesla Inc."),
        ("NFLX", "Netflix Inc."),
        ("CRM", "Salesforce Inc."),
        ("INTC", "Intel Corporation"),
        ("AMD", "Advanced Micro Devices")
    ]

    private var filteredSuggestions: [(symbol: String, name: String)] {
        let available = suggestedStocks.filter { !viewModel.watchlistStocks.contains($0.symbol) }

        if searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
            return available
        }

        return available.filter {
            $0.symbol.localizedCaseInsensitiveContains(searchQuery) ||
            $0.name.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    private var filteredSearchResults: [YahooFinanceService.SearchResult] {
        searchResults.filter { !viewModel.watchlistStocks.contains($0.symbol) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Add to Watchlist")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .padding(.horizontal, 16)
                .padding(.top, 16)

            Spacer().frame(height: 16)

            // Search field
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.gray)
                TextField("Search stocks...", text: $searchQuery)
                    .textFieldStyle(.plain)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.characters)
                    .foregroundStyle(.white)

                if isSearching {
                    ProgressView()
                        .scaleEffect(0.8)
                        .tint(.gray)
                }
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color(white: 0.12))
            )
            .padding(.horizontal, 16)
            .onChange(of: searchQuery) { _, newValue in
                debounceSearch(query: newValue)
            }

            Spacer().frame(height: 20)

            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    // Yahoo Finance search results
                    if !searchQuery.trimmingCharacters(in: .whitespaces).isEmpty && !filteredSearchResults.isEmpty {
                        Text("RESULTS")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundStyle(.gray)
                            .padding(.horizontal, 16)
                            .padding(.bottom, 8)

                        ForEach(filteredSearchResults) { result in
                            SearchResultRow(
                                symbol: result.symbol,
                                name: result.name
                            ) {
                                viewModel.addStock(result.symbol, name: result.name)
                            }

                            if result.id != filteredSearchResults.last?.id {
                                Divider()
                                    .background(Color(white: 0.2))
                                    .padding(.leading, 16)
                            }
                        }

                        if !filteredSuggestions.isEmpty {
                            Spacer().frame(height: 24)
                        }
                    }

                    // Suggestions section
                    if !filteredSuggestions.isEmpty {
                        Text(searchQuery.trimmingCharacters(in: .whitespaces).isEmpty ? "POPULAR" : "SUGGESTED")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundStyle(.gray)
                            .padding(.horizontal, 16)
                            .padding(.bottom, 8)

                        ForEach(filteredSuggestions, id: \.symbol) { stock in
                            SearchResultRow(
                                symbol: stock.symbol,
                                name: stock.name
                            ) {
                                viewModel.addStock(stock.symbol, name: stock.name)
                            }

                            if stock.symbol != filteredSuggestions.last?.symbol {
                                Divider()
                                    .background(Color(white: 0.2))
                                    .padding(.leading, 16)
                            }
                        }
                    }

                    // No results state
                    if filteredSuggestions.isEmpty && filteredSearchResults.isEmpty && !isSearching {
                        HStack {
                            Spacer()
                            Text("No stocks found")
                                .foregroundStyle(.gray)
                                .padding(32)
                            Spacer()
                        }
                    }
                }
            }
        }
        .background(Color.black)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func debounceSearch(query: String) {
        searchTask?.cancel()

        let trimmed = query.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else {
            searchResults = []
            isSearching = false
            return
        }

        searchTask = Task {
            try? await Task.sleep(nanoseconds: 300_000_000)
            guard !Task.isCancelled else { return }

            await MainActor.run { isSearching = true }

            do {
                let results = try await YahooFinanceService.shared.searchStocks(query: trimmed)
                guard !Task.isCancelled else { return }
                await MainActor.run {
                    searchResults = results
                    isSearching = false
                }
            } catch {
                guard !Task.isCancelled else { return }
                await MainActor.run { isSearching = false }
            }
        }
    }
}

struct SearchResultRow: View {
    let symbol: String
    let name: String
    let onAdd: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                Text(symbol)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                Text(name)
                    .font(.caption)
                    .foregroundStyle(.gray)
                    .lineLimit(1)
            }

            Spacer()

            Button(action: onAdd) {
                Image(systemName: "plus.circle.fill")
                    .font(.title3)
                    .foregroundStyle(.green)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
    }
}
