package com.fikirkulucka.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.ui.theme.DeepIndigo
import com.fikirkulucka.app.ui.theme.NeonCyan
import com.fikirkulucka.app.ui.theme.WarmAmber
import java.util.concurrent.TimeUnit

@Composable
fun IncubationCard(
    idea: IdeaEntity,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val progress = remember(idea.id, idea.createdAt, idea.matureAt) { idea.getProgress(now) }
    val isReady = now >= idea.matureAt

    val animatedProgress by animateFloatAsState(
        targetValue = if (isReady) 1f else progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    val pulseTransition = rememberInfiniteTransition(label = "cardPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val accentColor = when {
        isReady -> WarmAmber
        animatedProgress >= 0.5f -> NeonCyan
        else -> DeepIndigo
    }

    val remainingMillis = (idea.matureAt - now).coerceAtLeast(0L)
    val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF162033).copy(alpha = 0.94f),
                        Color(0xFF0D1321).copy(alpha = 0.98f)
                    ),
                    start = Offset.Zero,
                    end = Offset(500f, 500f)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = glowAlpha), Color.White.copy(alpha = 0.08f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        style = Stroke(width = 5.dp.toPx())
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = idea.content,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isReady) "✦ Kuluçka tamamlandı — Karar zamanı"
                    else "⏳ ${remainingHours}sa ${remainingMinutes}dk kaldı",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
