package com.cs407.tickertock.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cs407.tickertock.api.ApiKeyManager
import com.cs407.tickertock.data.DataManager
import com.cs407.tickertock.data.FakeDataProvider
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import com.cs407.tickertock.repository.StockRepository
import com.cs407.tickertock.ui.screens.AISummaryScreen
import com.cs407.tickertock.ui.screens.DetailedAISummaryScreen
import com.cs407.tickertock.ui.screens.NewsScreen
import com.cs407.tickertock.ui.screens.SearchScreen
import com.cs407.tickertock.ui.screens.SentimentHeatmapScreen
import com.cs407.tickertock.ui.screens.WatchlistScreen
import com.cs407.tickertock.ui.screens.WarRoomScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Watchlist : Screen("watchlist", "LIST", Icons.Default.List)
    object News : Screen("news", "NEWS", Icons.Default.Newspaper)
    object AISummary : Screen("ai_summary", "AI", Icons.Default.Analytics)
    object SentimentHeatmap : Screen("sentiment_heatmap", "MAP", Icons.Default.GridOn)
    object WarRoom : Screen("war_room", "WAR", Icons.Default.Dashboard)
    object DetailedAISummary : Screen("detailed_ai_summary/{stockSymbol}", "AI Detail", Icons.Default.Analytics)
    object Search : Screen("search", "Search", Icons.Default.List)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Watchlist,
        Screen.News,
        Screen.AISummary,
        Screen.SentimentHeatmap,
        Screen.WarRoom
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickerTockNavigation(
    navController: NavHostController,
    context: Context,
    modifier: Modifier = Modifier
) {
    val repository = remember { StockRepository.getInstance() }
    val dataManager = remember { DataManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Load persisted data on first launch OR use fake data
    var selectedStock by remember {
        mutableStateOf(
            if (FakeDataProvider.USE_FAKE_DATA) {
                FakeDataProvider.getWatchlistStocks().firstOrNull() ?: ""
            } else {
                dataManager.loadSelectedStock()
            }
        )
    }
    var watchlistStocks by remember {
        mutableStateOf(
            if (FakeDataProvider.USE_FAKE_DATA) {
                FakeDataProvider.getWatchlistStocks()
            } else {
                dataManager.loadWatchlist()
            }
        )
    }
    val maxWatchlistSize = 3

    // Stock data state
    var stockDataMap by remember {
        mutableStateOf(
            if (FakeDataProvider.USE_FAKE_DATA) {
                FakeDataProvider.getStockData()
            } else {
                dataManager.loadStockData()
            }
        )
    }

    // News data state
    var newsDataMap by remember {
        mutableStateOf(
            if (FakeDataProvider.USE_FAKE_DATA) {
                FakeDataProvider.getNewsArticles()
            } else {
                dataManager.loadNewsData()
            }
        )
    }

    // Loading states
    var isLoadingStock by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Track which articles have been swiped right for each stock
    var swipedArticles by remember {
        mutableStateOf(dataManager.loadSwipedArticles())
    }
    // Track article index per stock (persists across navigation)
    var articleIndexPerStock by remember {
        mutableStateOf(dataManager.loadArticleIndex())
    }
    // Track which stocks have shown the end message
    var endMessageShownForStocks by remember {
        mutableStateOf(dataManager.loadEndMessageShown())
    }

    // Save data whenever it changes
    LaunchedEffect(watchlistStocks) {
        dataManager.saveWatchlist(watchlistStocks)
    }
    LaunchedEffect(stockDataMap) {
        dataManager.saveStockData(stockDataMap)
    }
    LaunchedEffect(newsDataMap) {
        dataManager.saveNewsData(newsDataMap)
    }
    LaunchedEffect(swipedArticles) {
        dataManager.saveSwipedArticles(swipedArticles)
    }
    LaunchedEffect(articleIndexPerStock) {
        dataManager.saveArticleIndex(articleIndexPerStock)
    }
    LaunchedEffect(endMessageShownForStocks) {
        dataManager.saveEndMessageShown(endMessageShownForStocks)
    }
    LaunchedEffect(selectedStock) {
        dataManager.saveSelectedStock(selectedStock)
    }

    // Show loading or error overlay
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Watchlist.route
                ) {
                    composable(Screen.Watchlist.route) {
                        WatchlistScreen(
                            watchlistStocks = watchlistStocks,
                            stockDataMap = stockDataMap,
                            isRefreshing = isRefreshing,
                            onStockClick = { stock ->
                                selectedStock = stock
                                navController.navigate(Screen.News.route)
                            },
                            onSearchClick = {
                                if (watchlistStocks.size >= maxWatchlistSize) {
                                    errorMessage = "Maximum $maxWatchlistSize stocks allowed in watchlist"
                                } else {
                                    navController.navigate(Screen.Search.route)
                                }
                            },
                            onRefresh = {
                                if (watchlistStocks.isNotEmpty()) {
                                    isRefreshing = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = repository.refreshStockPrices(watchlistStocks)
                                        isRefreshing = false
                                        if (result.isSuccess) {
                                            val stocks = result.getOrThrow()
                                            val newStockDataMap = stockDataMap.toMutableMap()
                                            stocks.forEach { stock ->
                                                newStockDataMap[stock.symbol] = stock
                                            }
                                            stockDataMap = newStockDataMap
                                        } else {
                                            val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                                            if (errorMsg.contains("rate limit")) {
                                                errorMessage = "API key limit reached. Please try again later."
                                            } else {
                                                errorMessage = "Failed to refresh: $errorMsg"
                                            }
                                        }
                                    }
                                }
                            },
                            onStockRemove = { stockSymbol ->
                                // Remove from watchlist
                                watchlistStocks = watchlistStocks.filter { it != stockSymbol }

                                // Clear all data for this stock
                                swipedArticles = swipedArticles - stockSymbol
                                articleIndexPerStock = articleIndexPerStock - stockSymbol
                                endMessageShownForStocks = endMessageShownForStocks - stockSymbol
                                stockDataMap = stockDataMap - stockSymbol
                                newsDataMap = newsDataMap - stockSymbol
                                repository.clearCache(stockSymbol)
                                dataManager.clearStockData(stockSymbol)

                                // Update selected stock
                                if (selectedStock == stockSymbol) {
                                    selectedStock = if (watchlistStocks.isNotEmpty()) {
                                        watchlistStocks.first()
                                    } else {
                                        "" // Empty string when no stocks left
                                    }
                                }
                            }
                        )
                    }

                    composable(Screen.News.route) {
                        NewsScreen(
                            stockSymbol = selectedStock,
                            watchlistStocks = watchlistStocks,
                            newsDataMap = newsDataMap,
                            articleIndexPerStock = articleIndexPerStock,
                            endMessageShownForStocks = endMessageShownForStocks,
                            onAISummaryClick = {
                                // Navigate to detailed AI summary for the current stock
                                navController.navigate("detailed_ai_summary/$selectedStock")
                            },
                            onWarRoomClick = {
                                // Navigate to War Room for the current stock
                                navController.navigate(Screen.WarRoom.route)
                            },
                            onStockChange = { newStock ->
                                selectedStock = newStock
                            },
                            onArticleSwiped = { stockSymbol, articleId ->
                                val currentArticles = swipedArticles[stockSymbol] ?: emptySet()
                                swipedArticles = swipedArticles + (stockSymbol to (currentArticles + articleId))
                            },
                            onArticleIndexChanged = { stockSymbol, newIndex ->
                                articleIndexPerStock = articleIndexPerStock + (stockSymbol to newIndex)
                            },
                            onEndMessageShown = { stockSymbol ->
                                endMessageShownForStocks = endMessageShownForStocks + stockSymbol
                            }
                        )
                    }

                    composable(Screen.AISummary.route) {
                        AISummaryScreen(
                            swipedArticles = swipedArticles,
                            newsDataMap = newsDataMap,
                            onStockClick = { stockSymbol ->
                                navController.navigate("detailed_ai_summary/$stockSymbol")
                            }
                        )
                    }

                    composable(Screen.DetailedAISummary.route) { backStackEntry ->
                        val stockSymbol = backStackEntry.arguments?.getString("stockSymbol") ?: "NVDA"
                        DetailedAISummaryScreen(
                            stockSymbol = stockSymbol,
                            swipedArticles = swipedArticles,
                            newsDataMap = newsDataMap,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            watchlistStocks = watchlistStocks,
                            onStockAdd = { stockSymbol ->
                                if (stockSymbol !in watchlistStocks && watchlistStocks.size < maxWatchlistSize) {
                                    isLoadingStock = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = repository.fetchStockAndNews(stockSymbol)
                                        isLoadingStock = false
                                        if (result.isSuccess) {
                                            val (stock, news) = result.getOrThrow()
                                            // Add to watchlist
                                            watchlistStocks = watchlistStocks + stockSymbol
                                            // Store data
                                            stockDataMap = stockDataMap + (stockSymbol to stock)
                                            newsDataMap = newsDataMap + (stockSymbol to news)
                                            // Set as selected stock if it's the first one
                                            if (selectedStock.isEmpty()) {
                                                selectedStock = stockSymbol
                                            }
                                            navController.popBackStack()
                                        } else {
                                            val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                                            if (errorMsg.contains("rate limit")) {
                                                errorMessage = "API key limit reached. Please try again later."
                                            } else if (errorMsg.contains("No news available")) {
                                                errorMessage = "No news available for $stockSymbol"
                                            } else {
                                                errorMessage = "Failed to add stock: $errorMsg"
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    composable(Screen.SentimentHeatmap.route) {
                        SentimentHeatmapScreen(
                            watchlistStocks = watchlistStocks
                        )
                    }

                    composable(Screen.WarRoom.route) {
                        WarRoomScreen(
                            stock = stockDataMap[selectedStock],
                            newsArticles = newsDataMap[selectedStock] ?: emptyList(),
                            onBack = {
                                navController.navigate(Screen.Watchlist.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
            }
        }

        // Loading indicator
        if (isLoadingStock || isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error dialog
        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Error") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
    }

}
