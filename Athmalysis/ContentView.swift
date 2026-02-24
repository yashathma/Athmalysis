import SwiftUI

struct ContentView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var selectedTab = 0

    var body: some View {
        ZStack {
            TabView(selection: $selectedTab) {
                NavigationStack(path: $viewModel.watchlistNavPath) {
                    WatchlistView(selectedTab: $selectedTab)
                        .navigationDestination(for: WatchlistRoute.self) { route in
                            switch route {
                            case .search:
                                SearchView()
                            }
                        }
                }
                .tabItem {
                    Image(systemName: "list.bullet")
                    Text("LIST")
                }
                .tag(0)

                NavigationStack(path: $viewModel.newsNavPath) {
                    NewsView()
                        .navigationDestination(for: NewsRoute.self) { route in
                            switch route {
                            case .detailedAISummary(let symbol):
                                DetailedAISummaryView(stockSymbol: symbol)
                            }
                        }
                }
                .tabItem {
                    Image(systemName: "newspaper")
                    Text("NEWS")
                }
                .tag(1)

                NavigationStack(path: $viewModel.aiNavPath) {
                    AISummaryView()
                        .navigationDestination(for: AIRoute.self) { route in
                            switch route {
                            case .detailedAISummary(let symbol):
                                DetailedAISummaryView(stockSymbol: symbol)
                            }
                        }
                }
                .tabItem {
                    Image(systemName: "chart.bar")
                    Text("AI")
                }
                .tag(2)
            }

            // Loading overlay
            if viewModel.isLoadingStock || viewModel.isRefreshing {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()
                ProgressView()
                    .scaleEffect(1.5)
                    .tint(.white)
            }
        }
        .alert("Error", isPresented: Binding(
            get: { viewModel.errorMessage != nil },
            set: { if !$0 { viewModel.errorMessage = nil } }
        )) {
            Button("OK") { viewModel.errorMessage = nil }
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
    }
}

enum WatchlistRoute: Hashable {
    case search
}

enum NewsRoute: Hashable {
    case detailedAISummary(String)
}

enum AIRoute: Hashable {
    case detailedAISummary(String)
}
