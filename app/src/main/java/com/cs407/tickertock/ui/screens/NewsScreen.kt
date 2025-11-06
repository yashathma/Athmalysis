package com.cs407.tickertock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.Stock
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    stockSymbol: String,
    watchlistStocks: List<String>,
    newsDataMap: Map<String, List<NewsArticle>>,
    articleIndexPerStock: Map<String, Int>,
    endMessageShownForStocks: Set<String>,
    onAISummaryClick: () -> Unit,
    onWarRoomClick: () -> Unit,
    onStockChange: (String) -> Unit,
    onArticleSwiped: (String, String) -> Unit,
    onArticleIndexChanged: (String, Int) -> Unit,
    onEndMessageShown: (String) -> Unit
) {
    var currentStockIndex by remember(stockSymbol, watchlistStocks) {
        mutableStateOf(watchlistStocks.indexOf(stockSymbol).takeIf { it >= 0 } ?: 0)
    }

    // Get current article index for this stock
    val currentArticleIndex = articleIndexPerStock[stockSymbol] ?: 0
    val showEndMessage = stockSymbol in endMessageShownForStocks

    // Get articles from API data
    val articles = remember(stockSymbol, newsDataMap) {
        newsDataMap[stockSymbol] ?: emptyList()
    }

    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }

    LaunchedEffect(currentStockIndex, watchlistStocks) {
        if (currentStockIndex in watchlistStocks.indices) {
            onStockChange(watchlistStocks[currentStockIndex])
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(currentStockIndex, currentArticleIndex) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        // Determine if we should change stock or article based on total drag
                        if (abs(totalDragY) > abs(totalDragX) && abs(totalDragY) > 100) {
                            if (totalDragY > 0) {
                                // Swipe down - previous stock
                                if (currentStockIndex > 0) {
                                    currentStockIndex--
                                }
                            } else {
                                // Swipe up - next stock
                                if (currentStockIndex < watchlistStocks.size - 1) {
                                    currentStockIndex++
                                }
                            }
                        } else if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 100) {
                            if (!showEndMessage) {
                                if (totalDragX > 0) {
                                    // Swipe right - include article
                                    if (articles.isNotEmpty() && currentArticleIndex < articles.size) {
                                        val currentArticle = articles[currentArticleIndex]
                                        onArticleSwiped(stockSymbol, currentArticle.id)

                                        if (currentArticleIndex == articles.size - 1) {
                                            // Just swiped on the last article - show end message
                                            onEndMessageShown(stockSymbol)
                                        } else {
                                            onArticleIndexChanged(stockSymbol, currentArticleIndex + 1)
                                        }
                                    }
                                } else {
                                    // Swipe left - skip article
                                    if (articles.isNotEmpty()) {
                                        if (currentArticleIndex == articles.size - 1) {
                                            // Just swiped on the last article - show end message
                                            onEndMessageShown(stockSymbol)
                                        } else {
                                            onArticleIndexChanged(stockSymbol, currentArticleIndex + 1)
                                        }
                                    }
                                }
                            }
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    }
                ) { change, dragAmount ->
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y
                    change.consume()
                }
            }
    ) {
        // Check if watchlist is empty
        if (watchlistStocks.isEmpty() || stockSymbol.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your watchlist is empty.\nAdd stocks to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Stock symbol header (centered)
            Text(
                text = stockSymbol,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stock navigation indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Previous stock",
                    tint = if (currentStockIndex > 0) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Swipe up/down to change stocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Next stock",
                    tint = if (currentStockIndex < watchlistStocks.size - 1) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Article display or AI Summary button
        if (showEndMessage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onAISummaryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Generate AI Summary",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        } else if (articles.isNotEmpty() && currentArticleIndex < articles.size) {
            val currentArticle = articles[currentArticleIndex]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = currentArticle.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${currentArticle.publishedAt} by ${currentArticle.publisher}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentArticle.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gesture instructions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwipeLeft,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwipeRight,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Include",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        // Article counter
        if (articles.isNotEmpty() && !showEndMessage) {
            Text(
                text = "${currentArticleIndex + 1} of ${articles.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        }
    }
}