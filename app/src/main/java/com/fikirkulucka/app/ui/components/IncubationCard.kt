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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun IncubationCard(
    idea: IdeaEntity,
    onDecideNow: (IdeaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val progress = remember(idea.id, idea.createdAt, idea.matureAt) { idea.getProgress(now) }
    val isReady = now >= idea.matureAt

    val animatedProgress by animateFloatAsState(
        targetValue = if (isReady) 1f else progress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    val pulseTransition = rememberInfiniteTransition(label = "cardPulse")
    val ringScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    val accentColor = when {
        isReady -> SunnyAmber
        animatedProgress >= 0.5f -> EmeraldMint
        else -> VibrantCobalt
    }

    val remainingMillis = (idea.matureAt - now).coerceAtLeast(0L)
    val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PureCard)
            .border(1.2.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Canvas(modifier = Modifier.size(56.dp * ringScale)) {
                    drawCircle(
                        color = BorderSoft,
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
                    color = InkDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = idea.content,
                    color = InkDark,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 21.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isReady) "✦ Kuluçka tamamlandı — Karar vakti"
                    else "⏳ ${remainingHours}sa${remainingMinutes}dk kaldı",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material3.TextButton(onClick = { onDecideNow(idea) }) {
                Text(
                    text = "Şimdi Karar Ver →",
                    color = VibrantCobalt,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
