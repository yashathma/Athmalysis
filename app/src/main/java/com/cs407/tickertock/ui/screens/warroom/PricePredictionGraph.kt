package com.cs407.tickertock.ui.screens.warroom

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.PricePrediction
import kotlin.math.max
import kotlin.math.min

/**
 * Price prediction graph showing future price projections with confidence ranges
 */
@Composable
fun PricePredictionGraph(
    predictions: List<PricePrediction>,
    modifier: Modifier = Modifier
) {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animationProgress = value
        }
    }

    // Pulsing animation for AI glow
    val infiniteTransition = rememberInfiniteTransition(label = "ai_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title Badge
        Surface(
            color = Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.drawBehind {
                // Subtle glow effect
                drawRoundRect(
                    color = Color.White.copy(alpha = glowAlpha * 0.08f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRICE PREDICTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No prediction data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            // Current price and prediction summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", predictions.first().predictedPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1 Week Est.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    val lastPrediction = predictions.last()
                    val firstPrice = predictions.first().predictedPrice
                    val lastPrice = lastPrediction.predictedPrice
                    val change = lastPrice - firstPrice
                    val changePercent = (change / firstPrice) * 100

                    Text(
                        text = "$${String.format("%.2f", lastPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (change >= 0) Color(0xFF00FF41) else Color(0xFFFF4444),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (change >= 0) "+" else ""}${String.format("%.2f", changePercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (change >= 0) Color(0xFF00FF41) else Color(0xFFFF4444),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPriceGraph(
                        predictions = predictions,
                        animationProgress = animationProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                predictions.forEach { prediction ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = prediction.timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${(prediction.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00FF41).copy(alpha = prediction.confidence),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence legend
            Text(
                text = "Confidence decreases over longer time horizons",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 9.sp
            )
        }
    }
}

private fun DrawScope.drawPriceGraph(
    predictions: List<PricePrediction>,
    animationProgress: Float
) {
    if (predictions.isEmpty()) return

    val padding = 40f
    val graphWidth = size.width - (padding * 2)
    val graphHeight = size.height - (padding * 2)

    // Calculate price range
    val allPrices = predictions.flatMap { listOf(it.predictedPrice, it.range.low, it.range.high) }
    val minPrice = allPrices.minOrNull() ?: 0.0
    val maxPrice = allPrices.maxOrNull() ?: 100.0
    val priceRange = maxPrice - minPrice
    val priceRangeWithPadding = priceRange * 1.2

    fun priceToY(price: Double): Float {
        val normalizedPrice = (price - minPrice) / priceRangeWithPadding
        return size.height - padding - (normalizedPrice.toFloat() * graphHeight)
    }

    fun indexToX(index: Int): Float {
        val step = graphWidth / (predictions.size - 1).coerceAtLeast(1)
        return padding + (index * step)
    }

    // Draw grid lines
    for (i in 0..4) {
        val y = padding + (graphHeight * i / 4)
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(padding, y),
            end = Offset(size.width - padding, y),
            strokeWidth = 1f
        )
    }

    // Draw confidence range (filled area)
    val confidencePath = Path()
    val visiblePoints = (predictions.size * animationProgress).toInt().coerceAtLeast(1)

    // Top of range
    predictions.take(visiblePoints).forEachIndexed { index, prediction ->
        val x = indexToX(index)
        val y = priceToY(prediction.range.high)

        if (index == 0) {
            confidencePath.moveTo(x, y)
        } else {
            confidencePath.lineTo(x, y)
        }
    }

    // Bottom of range (reverse)
    predictions.take(visiblePoints).reversed().forEachIndexed { index, prediction ->
        val reverseIndex = visiblePoints - 1 - index
        val x = indexToX(reverseIndex)
        val y = priceToY(prediction.range.low)
        confidencePath.lineTo(x, y)
    }
    confidencePath.close()

    drawPath(
        path = confidencePath,
        color = Color(0xFF00FF41).copy(alpha = 0.1f)
    )

    // Draw prediction line
    val predictionPath = Path()
    predictions.take(visiblePoints).forEachIndexed { index, prediction ->
        val x = indexToX(index)
        val y = priceToY(prediction.predictedPrice)

        if (index == 0) {
            predictionPath.moveTo(x, y)
        } else {
            predictionPath.lineTo(x, y)
        }
    }

    // Gradient stroke for prediction line
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00FF41),
            Color(0xFF00FF41).copy(alpha = 0.6f)
        ),
        startX = padding,
        endX = size.width - padding
    )

    drawPath(
        path = predictionPath,
        brush = gradient,
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )

    // Draw data points
    predictions.take(visiblePoints).forEachIndexed { index, prediction ->
        val x = indexToX(index)
        val y = priceToY(prediction.predictedPrice)

        // Outer glow
        drawCircle(
            color = Color(0xFF00FF41).copy(alpha = prediction.confidence * 0.3f),
            radius = 8f,
            center = Offset(x, y)
        )

        // Inner point
        drawCircle(
            color = Color(0xFF00FF41).copy(alpha = prediction.confidence),
            radius = 4f,
            center = Offset(x, y)
        )

        // Center dot
        drawCircle(
            color = Color.White,
            radius = 2f,
            center = Offset(x, y)
        )
    }

    // Draw current price indicator (vertical line at first point)
    if (visiblePoints > 0) {
        val x = indexToX(0)
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(x, padding),
            end = Offset(x, size.height - padding),
            strokeWidth = 1f
        )
    }
}
