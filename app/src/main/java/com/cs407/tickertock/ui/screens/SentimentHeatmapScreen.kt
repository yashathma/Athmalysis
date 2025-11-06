package com.cs407.tickertock.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.tickertock.data.FakeDataProvider
import com.cs407.tickertock.data.SentimentArticle
import com.cs407.tickertock.data.StockSentiment
import com.cs407.tickertock.viewmodel.SentimentHeatmapViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentHeatmapScreen(
    watchlistStocks: List<String>,
    modifier: Modifier = Modifier,
    viewModel: SentimentHeatmapViewModel = viewModel()
) {
    // Use fake data if toggle is enabled
    val displaySentiments = if (FakeDataProvider.USE_FAKE_DATA) {
        watchlistStocks.mapNotNull { symbol ->
            FakeDataProvider.getSentimentForStock(symbol)?.let { sentiment ->
                symbol to sentiment
            }
        }.toMap()
    } else {
        val uiState by viewModel.uiState.collectAsState()

        // Load sentiments when watchlist changes
        LaunchedEffect(watchlistStocks) {
            viewModel.loadSentiments(watchlistStocks)
        }

        uiState.sentiments
    }

    val isLoading = if (FakeDataProvider.USE_FAKE_DATA) false else {
        val uiState by viewModel.uiState.collectAsState()
        uiState.isLoading
    }

    val isRefreshing = if (FakeDataProvider.USE_FAKE_DATA) false else {
        val uiState by viewModel.uiState.collectAsState()
        uiState.isRefreshing
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sentiment Heatmap",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshSentiments(watchlistStocks) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    if (!FakeDataProvider.USE_FAKE_DATA) {
                        viewModel.refreshSentiments(watchlistStocks)
                    }
                }
            ) {
                when {
                    isLoading -> {
                        LoadingView()
                    }
                    watchlistStocks.isEmpty() -> {
                        EmptyWatchlistView()
                    }
                    displaySentiments.isEmpty() && !isLoading -> {
                        EmptyDataView()
                    }
                    else -> {
                        val selectedStock = if (FakeDataProvider.USE_FAKE_DATA) {
                            null
                        } else {
                            val uiState by viewModel.uiState.collectAsState()
                            uiState.selectedStock
                        }

                        HeatmapContent(
                            sentiments = displaySentiments,
                            selectedStock = selectedStock,
                            onStockClick = { symbol ->
                                if (!FakeDataProvider.USE_FAKE_DATA) {
                                    viewModel.selectStock(symbol)
                                }
                            },
                            onDismissDetail = {
                                if (!FakeDataProvider.USE_FAKE_DATA) {
                                    viewModel.deselectStock()
                                }
                            }
                        )
                    }
                }
            }

            // Error dialog (only for real data)
            if (!FakeDataProvider.USE_FAKE_DATA) {
                val uiState by viewModel.uiState.collectAsState()
                uiState.errorMessage?.let { message ->
                    AlertDialog(
                        onDismissRequest = { viewModel.clearError() },
                        title = { Text("Error") },
                        text = { Text(message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyWatchlistView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No stocks in watchlist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add stocks to see sentiment analysis",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyDataView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No sentiment data available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeatmapContent(
    sentiments: Map<String, StockSentiment>,
    selectedStock: String?,
    onStockClick: (String) -> Unit,
    onDismissDetail: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Grid view
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = sentiments.entries.sortedBy { it.key }.toList(),
                key = { it.key }
            ) { (symbol, sentiment) ->
                SentimentCard(
                    symbol = symbol,
                    sentiment = sentiment,
                    onClick = { onStockClick(symbol) },
                    isSelected = symbol == selectedStock
                )
            }
        }

        // Expanded detail view
        AnimatedVisibility(
            visible = selectedStock != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut()
        ) {
            selectedStock?.let { symbol ->
                sentiments[symbol]?.let { sentiment ->
                    SentimentDetailView(
                        sentiment = sentiment,
                        onDismiss = onDismissDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun SentimentCard(
    symbol: String,
    sentiment: StockSentiment,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    // Animated color based on sentiment
    val targetColor = getSentimentColor(sentiment.sentimentScore)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(800, easing = EaseInOutCubic),
        label = "sentiment_color"
    )

    // Pulsing animation for selected card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor.copy(alpha = 0.9f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = sentiment.sentimentLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format("%.2f", sentiment.sentimentScore),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${sentiment.articleCount} articles",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SentimentDetailView(
    sentiment: StockSentiment,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sentiment.stockSymbol,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sentiment.sentimentLabel,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getSentimentColor(sentiment.sentimentScore)
                )
            )

            // Sentiment score visualization
            SentimentScoreBar(sentiment.sentimentScore)

            // Articles list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Supporting Articles (${sentiment.articleCount})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(sentiment.supportingArticles) { article ->
                    ArticleCard(article)
                }
            }
        }
    }
}

@Composable
private fun SentimentScoreBar(score: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bearish", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                String.format("Score: %.2f", score),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Bullish", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        0f to Color(0xFFE53935), // Red (Bearish)
                        0.5f to Color(0xFFFDD835), // Yellow (Neutral)
                        1f to Color(0xFF43A047)  // Green (Bullish)
                    )
                )
        ) {
            // Position indicator
            val position = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (position * 100).dp)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun ArticleCard(article: SentimentArticle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = getSentimentColor(article.sentimentScore).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = article.sentimentLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = getSentimentColor(article.sentimentScore)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.publisher,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Relevance: ${String.format("%.0f%%", article.relevanceScore * 100)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = article.publishedAt,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get color based on sentiment score
 * Score range: -1.0 (very negative) to 1.0 (very positive)
 */
private fun getSentimentColor(score: Double): Color {
    return when {
        score >= 0.35 -> Color(0xFF43A047)  // Strong Bullish - Green
        score >= 0.15 -> Color(0xFF66BB6A)  // Somewhat Bullish - Light Green
        score >= -0.15 -> Color(0xFFFDD835) // Neutral - Yellow
        score >= -0.35 -> Color(0xFFFF9800) // Somewhat Bearish - Orange
        else -> Color(0xFFE53935)           // Bearish - Red
    }
}
