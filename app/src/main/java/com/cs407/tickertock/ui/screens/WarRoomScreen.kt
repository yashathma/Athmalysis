package com.cs407.tickertock.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cs407.tickertock.data.Stock
import com.cs407.tickertock.data.NewsArticle
import com.cs407.tickertock.data.WarRoomAnalysis
import com.cs407.tickertock.data.FakeWarRoomData
import com.cs407.tickertock.data.FakeDataProvider
import com.cs407.tickertock.ui.screens.warroom.*

/**
 * War Room Dashboard - High-tech AI analysis interface
 * Dark theme with multiple synchronized AI analysis panels
 *
 * TO SWITCH BACK TO REAL DATA:
 * Set useFakeData = false and ensure stock and newsArticles are passed from navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarRoomScreen(
    stock: Stock?,
    newsArticles: List<NewsArticle>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    useFakeData: Boolean = FakeDataProvider.USE_FAKE_DATA
) {
    // Use fake data based on master toggle
    val displayStock = if (useFakeData) FakeWarRoomData.getFakeStock() else stock
    val displayArticles = if (useFakeData) FakeWarRoomData.getFakeNewsArticles() else newsArticles

    val analysis = remember(displayStock, displayArticles) {
        if (displayStock != null) {
            WarRoomAnalysis.generate(displayStock, displayArticles)
        } else {
            null
        }
    }

    // War Room background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF121212),
                        Color(0xFF0A0A0A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            WarRoomHeader(
                stockSymbol = displayStock?.symbol ?: "---",
                stockName = displayStock?.name ?: "No Stock Selected",
                currentPrice = displayStock?.currentPrice ?: 0.0,
                priceChange = displayStock?.priceChange ?: 0.0,
                percentageChange = displayStock?.percentageChange ?: 0.0,
                onBack = onBack
            )

            if (analysis != null) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // News Timeline at Top - Full Width
                    NewsImpactTimeline(
                        newsImpacts = analysis.newsImpacts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    )

                    // Main content grid below
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top row: Sentiment & Risk
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WarRoomPanel(
                                title = "Sentiment",
                                modifier = Modifier.weight(1f)
                            ) {
                                SentimentGauge(
                                    sentiment = analysis.sentiment,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            WarRoomPanel(
                                title = "Risk",
                                modifier = Modifier.weight(1f)
                            ) {
                                RiskRadarChart(
                                    risk = analysis.risk,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Bottom row: Prediction only
                        WarRoomPanel(
                            title = "Prediction",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PricePredictionGraph(
                                predictions = analysis.pricePredictions,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NO STOCK SELECTED",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF00FF41),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "Select a stock from the watchlist to view AI analysis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WarRoomHeader(
    stockSymbol: String,
    stockName: String,
    currentPrice: Double,
    priceChange: Double,
    percentageChange: Double,
    onBack: () -> Unit
) {
    Surface(
        color = Color(0xFF0F0F0F),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern title
                Text(
                    text = "WAR ROOM",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stock info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stockSymbol,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stockName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", currentPrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    val isPositive = priceChange >= 0
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", priceChange)} (${if (isPositive) "+" else ""}${String.format("%.2f", percentageChange)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPositive) Color(0xFF00FF41) else Color(0xFFFF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WarRoomPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF1A1A1A),
        shadowElevation = 4.dp,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF0F0F0F)
                        )
                    )
                )
        ) {
            Column {
                content()
            }
        }
    }
}

enum class WarRoomPanel(val title: String) {
    SENTIMENT("SENTIMENT ANALYSIS"),
    RISK("RISK ASSESSMENT"),
    NEWS_IMPACT("NEWS IMPACT TIMELINE"),
    PREDICTION("PRICE PREDICTION")
}
