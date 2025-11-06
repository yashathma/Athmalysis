package com.cs407.tickertock.ui.screens.warroom

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.tickertock.data.SentimentAnalysis
import kotlin.math.cos
import kotlin.math.sin

/**
 * Speedometer-style sentiment gauge for War Room Dashboard
 * Shows sentiment from -100 (bearish) to +100 (bullish)
 */
@Composable
fun SentimentGauge(
    sentiment: SentimentAnalysis,
    modifier: Modifier = Modifier
) {
    // Animate the needle movement
    val animatedScore by animateFloatAsState(
        targetValue = sentiment.score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sentiment_score"
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
        // Title Badge - Centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
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
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SENTIMENT ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speedometer - adjusted height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSpeedometer(
                    score = animatedScore,
                    confidence = sentiment.confidence
                )
            }

            // Center value display - positioned at bottom with more clearance
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${animatedScore.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Confidence: ${(sentiment.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 7.sp
                )
                Text(
                    text = sentiment.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = getSentimentColor(animatedScore),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    maxLines = 1
                )
            }
        }
    }
}

private fun DrawScope.drawSpeedometer(score: Float, confidence: Float) {
    val width = size.width
    val height = size.height
    val center = Offset(width / 2, height * 0.38f) // Moved even higher to avoid overlap
    val radius = width * 0.22f // Smaller radius to make more room

    // Draw arc background (dark)
    drawArc(
        color = Color(0xFF1A1A1A),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 30f, cap = StrokeCap.Round)
    )

    // Draw colored sentiment arc
    val gradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFFF4444), // Red (bearish)
            Color(0xFFFFAA00), // Orange (neutral)
            Color(0xFF00FF41)  // Green (bullish)
        ),
        center = center
    )

    drawArc(
        brush = gradient,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 25f, cap = StrokeCap.Round)
    )

    // Draw tick marks
    for (i in 0..10) {
        val angle = 180f + (i * 18f) // 180 degrees total, 10 segments
        val startRadius = radius - 15f
        val endRadius = radius + 15f

        val startX = center.x + startRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val startY = center.y + startRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
        val endX = center.x + endRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val endY = center.y + endRadius * sin(Math.toRadians(angle.toDouble())).toFloat()

        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2f
        )
    }

    // Draw needle
    val needleAngle = 180f + ((score + 100f) / 200f * 180f) // Map -100 to +100 to 180 to 360 degrees
    val needleLength = radius * 0.9f

    rotate(needleAngle, center) {
        // Needle shadow
        drawLine(
            color = Color.Black.copy(alpha = 0.5f),
            start = center + Offset(2f, 2f),
            end = center + Offset(needleLength + 2f, 2f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Needle
        drawLine(
            color = getSentimentColor(score),
            start = center,
            end = center + Offset(needleLength, 0f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    // Draw center circle
    drawCircle(
        color = Color(0xFF2A2A2A),
        radius = 12f,
        center = center
    )

    // Draw confidence ring around center
    drawCircle(
        color = Color(0xFF00FF41).copy(alpha = confidence),
        radius = 8f,
        center = center
    )
}

private fun getSentimentColor(score: Float): Color {
    return when {
        score >= 20 -> Color(0xFF00FF41) // Bullish green
        score >= -20 -> Color(0xFFFFAA00) // Neutral orange
        else -> Color(0xFFFF4444) // Bearish red
    }
}
