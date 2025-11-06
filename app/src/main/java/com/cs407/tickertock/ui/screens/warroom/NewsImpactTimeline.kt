package com.cs407.tickertock.ui.screens.warroom

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.NewsImpact
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Timeline visualization showing news impact events
 */
@Composable
fun NewsImpactTimeline(
    newsImpacts: List<NewsImpact>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Top border line
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
        ) {
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1f
            )
        }

        // Bottom border line
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
        ) {
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1f
            )
        }

        // Scrolling content
        if (newsImpacts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No news data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            // Horizontal timeline with auto-scroll to the left
            val listState = rememberLazyListState()

            // Multiply items many times for seamless infinite loop effect
            val infiniteItems = newsImpacts + newsImpacts + newsImpacts + newsImpacts + newsImpacts + newsImpacts

            // Auto-scroll effect - smooth continuous scroll
            LaunchedEffect(newsImpacts) {
                if (newsImpacts.isNotEmpty()) {
                    // Start from the second set to allow seamless wrapping
                    listState.scrollToItem(newsImpacts.size, 0)

                    while (isActive) {
                        // Use scroll() to perform smooth pixel-by-pixel scrolling
                        listState.scroll {
                            // Scroll by 2 pixels
                            scrollBy(2f)
                        }

                        // Check if we need to wrap around seamlessly
                        val currentIndex = listState.firstVisibleItemIndex

                        // If we've scrolled past 4 sets, instantly jump back to the 2nd set
                        // This happens off-screen so it's invisible to the user
                        if (currentIndex >= newsImpacts.size * 4) {
                            val currentOffset = listState.firstVisibleItemScrollOffset
                            listState.scrollToItem(newsImpacts.size, currentOffset)
                        }

                        delay(16) // ~60fps
                    }
                }
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false, // Disable manual scrolling
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(infiniteItems.size) { index ->
                    NewsImpactItem(impact = infiniteItems[index])
                }
            }
        }
    }
}

@Composable
private fun NewsImpactItem(impact: NewsImpact) {
    // Animate impact bar
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedImpact by animateFloatAsState(
        targetValue = if (animationPlayed) impact.impact else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "impact_animation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    // Horizontal card layout
    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top row: time and impact marker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Time indicator
            Text(
                text = impact.relativeTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 9.sp
            )

            // Impact marker circle
            Canvas(modifier = Modifier.size(16.dp)) {
                val color = getImpactColor(impact.impact)

                // Outer glow
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = size.minDimension / 2
                )

                // Inner circle
                drawCircle(
                    color = color,
                    radius = size.minDimension / 3
                )
            }

            // Impact label
            Text(
                text = impact.impactLabel,
                style = MaterialTheme.typography.labelSmall,
                color = getImpactColor(impact.impact),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        // News title
        Text(
            text = impact.title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )

        // Impact strength indicator
        ImpactBar(impact = animatedImpact)
    }
}

@Composable
private fun ImpactBar(impact: Float) {
    val absImpact = kotlin.math.abs(impact)
    val barWidth = 60.dp

    Box(
        modifier = Modifier
            .width(barWidth)
            .height(6.dp)
    ) {
        // Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                size = size
            )
        }

        // Filled portion
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(absImpact)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = getImpactColor(impact),
                    size = size
                )
            }
        }
    }
}

private fun getImpactColor(impact: Float): Color {
    return when {
        impact >= 0.6f -> Color(0xFF00FF41)  // Major Positive - Bright Green
        impact >= 0.2f -> Color(0xFF88FF88)  // Minor Positive - Light Green
        impact >= -0.2f -> Color(0xFFFFAA00) // Neutral - Orange
        impact >= -0.6f -> Color(0xFFFF8844) // Minor Negative - Light Red
        else -> Color(0xFFFF4444)            // Major Negative - Red
    }
}
