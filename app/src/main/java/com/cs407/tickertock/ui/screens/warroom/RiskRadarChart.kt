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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.RiskAssessment
import kotlin.math.cos
import kotlin.math.sin

/**
 * Radar/Spider chart showing multi-dimensional risk assessment
 */
@Composable
fun RiskRadarChart(
    risk: RiskAssessment,
    modifier: Modifier = Modifier
) {
    // Animate the risk values
    val animatedVolatility by animateFloatAsState(
        targetValue = risk.volatilityRisk,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "volatility"
    )
    val animatedMarket by animateFloatAsState(
        targetValue = risk.marketRisk,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "market"
    )
    val animatedNews by animateFloatAsState(
        targetValue = risk.newsRisk,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "news"
    )
    val animatedTechnical by animateFloatAsState(
        targetValue = risk.technicalRisk,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "technical"
    )
    val animatedSentiment by animateFloatAsState(
        targetValue = risk.sentimentRisk,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "sentiment"
    )

    val riskValues = listOf(
        animatedVolatility,
        animatedMarket,
        animatedNews,
        animatedTechnical,
        animatedSentiment
    )

    val riskLabels = listOf(
        "Volatility",
        "Market",
        "News",
        "Technical",
        "Sentiment"
    )

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "RISK ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Risk level badge
        val riskColor = getRiskColor(risk.overallRisk)
        Text(
            text = risk.riskLevel,
            style = MaterialTheme.typography.titleMedium,
            color = riskColor,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${(risk.overallRisk * 100).toInt()}% Risk Level",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Radar chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRadarChart(
                    values = riskValues,
                    labels = riskLabels
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Individual risk metrics
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RiskMetricRow("Volatility", animatedVolatility)
            RiskMetricRow("Market", animatedMarket)
            RiskMetricRow("News", animatedNews)
            RiskMetricRow("Technical", animatedTechnical)
            RiskMetricRow("Sentiment", animatedSentiment)
        }
    }
}

private fun DrawScope.drawRadarChart(
    values: List<Float>,
    labels: List<String>
) {
    val center = Offset(size.width / 2, size.height / 2)
    val maxRadius = size.minDimension / 2.5f
    val numberOfAxes = values.size
    val angleStep = 360f / numberOfAxes

    // Draw concentric circles (grid)
    for (level in 1..5) {
        val radius = maxRadius * (level / 5f)
        drawCircle(
            color = Color.White.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Stroke(width = 1f)
        )
    }

    // Draw axes
    for (i in 0 until numberOfAxes) {
        val angle = Math.toRadians((i * angleStep - 90).toDouble())
        val endX = center.x + (maxRadius * cos(angle)).toFloat()
        val endY = center.y + (maxRadius * sin(angle)).toFloat()

        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1f
        )
    }

    // Draw risk polygon
    val path = Path()
    for (i in 0 until numberOfAxes) {
        val angle = Math.toRadians((i * angleStep - 90).toDouble())
        val radius = maxRadius * values[i]
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    // Fill - subtle transparent
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.08f)
    )

    // Stroke - brighter white shade
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = 1.5f)
    )

    // Draw data points with metric-specific colors
    val metricColors = listOf(
        Color(0xFFFF4444),  // Volatility - Red
        Color(0xFFFFAA00),  // Market - Orange
        Color(0xFFFFDD44),  // News - Yellow
        Color(0xFF00CCFF),  // Technical - Cyan
        Color(0xFFAA44FF)   // Sentiment - Purple
    )

    for (i in 0 until numberOfAxes) {
        val angle = Math.toRadians((i * angleStep - 90).toDouble())
        val radius = maxRadius * values[i]
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        val pointColor = metricColors.getOrElse(i) { Color(0xFFFF4444) }

        drawCircle(
            color = pointColor,
            radius = 6f,
            center = Offset(x, y)
        )

        // Glow effect
        drawCircle(
            color = pointColor.copy(alpha = 0.3f),
            radius = 10f,
            center = Offset(x, y)
        )
    }
}

@Composable
private fun RiskMetricRow(label: String, value: Float) {
    val metricColor = getMetricColor(label)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label text on top of the bar
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )

        // Bar and percentage in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Horizontal risk bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
            ) {
                // Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFF1A1A1A),
                            size = size
                        )
                    }
                }

                // Filled portion
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(value)
                        .padding(end = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = metricColor,
                            size = size
                        )
                    }
                }
            }

            // Percentage value
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = metricColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(45.dp)
            )
        }
    }
}

private fun getMetricColor(metricName: String): Color {
    return when (metricName) {
        "Volatility" -> Color(0xFFFF4444)  // Red
        "Market" -> Color(0xFFFFAA00)      // Orange
        "News" -> Color(0xFFFFDD44)        // Yellow
        "Technical" -> Color(0xFF00CCFF)   // Cyan
        "Sentiment" -> Color(0xFFAA44FF)   // Purple
        else -> Color(0xFF00FF41)          // Green default
    }
}

private fun getRiskColor(risk: Float): Color {
    return when {
        risk >= 0.75f -> Color(0xFFFF0000) // Critical - Red
        risk >= 0.5f -> Color(0xFFFF4444)  // High - Orange-Red
        risk >= 0.3f -> Color(0xFFFFAA00)  // Moderate - Orange
        else -> Color(0xFF00FF41)          // Low - Green
    }
}
