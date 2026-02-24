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
                .font(.title)
                .fontWeight(.bold)
                .padding(.horizontal, 16)
                .padding(.top, 16)

            Spacer().frame(height: 16)

            // Search field
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.secondary)
                TextField("Search any stock ticker...", text: $searchQuery)
                    .textFieldStyle(.plain)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.characters)

                if isSearching {
                    ProgressView()
                        .scaleEffect(0.8)
                }
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(Color.secondary.opacity(0.3), lineWidth: 1)
            )
            .padding(.horizontal, 16)
            .onChange(of: searchQuery) { _, newValue in
                debounceSearch(query: newValue)
            }

            Spacer().frame(height: 16)

            ScrollView {
                LazyVStack(alignment: .leading, spacing: 8) {
                    // Yahoo Finance search results (shown when actively searching)
                    if !searchQuery.trimmingCharacters(in: .whitespaces).isEmpty && !filteredSearchResults.isEmpty {
                        Text("Search Results")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 16)

                        ForEach(filteredSearchResults) { result in
                            SearchResultCard(
                                symbol: result.symbol,
                                name: result.name
                            ) {
                                viewModel.addStock(result.symbol, name: result.name)
                            }
                        }

                        if !filteredSuggestions.isEmpty {
                            Spacer().frame(height: 16)
                        }
                    }

                    // Suggestions section
                    if !filteredSuggestions.isEmpty {
                        Text(searchQuery.trimmingCharacters(in: .whitespaces).isEmpty ? "Suggestions" : "Suggested Matches")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 16)

                        ForEach(filteredSuggestions, id: \.symbol) { stock in
                            SearchResultCard(
                                symbol: stock.symbol,
                                name: stock.name
                            ) {
                                viewModel.addStock(stock.symbol, name: stock.name)
                            }
                        }
                    }

                    // No results state
                    if filteredSuggestions.isEmpty && filteredSearchResults.isEmpty && !isSearching {
                        HStack {
                            Spacer()
                            Text("No stocks found")
                                .foregroundStyle(.secondary)
                                .padding(32)
                            Spacer()
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
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
            // Debounce 300ms
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

struct SearchResultCard: View {
    let symbol: String
    let name: String
    let onAdd: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(symbol)
                    .font(.headline)
                    .fontWeight(.bold)
                Text(name)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button(action: onAdd) {
                Image(systemName: "plus")
                    .foregroundStyle(.blue)
                    .frame(width: 44, height: 44)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.08), radius: 2, y: 1)
        )
    }
}
