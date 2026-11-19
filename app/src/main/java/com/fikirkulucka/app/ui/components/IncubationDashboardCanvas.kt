package com.fikirkulucka.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun IncubationDashboardCanvas(
    activeIdeas: List<IdeaEntity>,
    approvedCount: Int,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "liveGraph")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val pulseScale by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val now = System.currentTimeMillis()
    val readyCount = activeIdeas.count { now >= it.matureAt }
    val midCount = activeIdeas.count { val p = it.getProgress(now); p in 0.35f..0.99f && now < it.matureAt }
    val earlyCount = activeIdeas.size - readyCount - midCount

    val total = (earlyCount + midCount + readyCount).coerceAtLeast(1)
    val earlySweep = 360f * (earlyCount.toFloat() / total)
    val midSweep = 360f * (midCount.toFloat() / total)
    val readySweep = 360f * (readyCount.toFloat() / total)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFEEF4FF))
                )
            )
            .border(1.dp, BorderSoft, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(84.dp * pulseScale)) {
                    val strokeW = 9.dp.toPx()
                    val pad = strokeW / 2f
                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                    drawArc(
                        color = BorderSoft,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )
                    if (activeIdeas.isNotEmpty()) {
                        var start = -90f
                        if (earlyCount > 0) {
                            drawArc(VibrantCobalt, start, earlySweep, false, Offset(pad, pad), arcSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                            start += earlySweep
                        }
                        if (midCount > 0) {
                            drawArc(EmeraldMint, start, midSweep, false, Offset(pad, pad), arcSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                            start += midSweep
                        }
                        if (readyCount > 0) {
                            drawArc(SunnyAmber, start, readySweep, false, Offset(pad, pad), arcSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeIdeas.size}",
                        color = InkDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Kuluçka",
                        color = SlateMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Canlı Kuluçka Nabzı",
                    color = InkDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Yeni: $earlyCount • Gelişen: $midCount • Hazır: $readyCount",
                    color = SlateMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Onaylanan & Yol Haritası: $approvedCount fikir",
                    color = EmeraldMint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }

        // Hareketli sinüs dalga grafiği
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
        ) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val amp = h * 0.28f
            val path = Path()
            val steps = 48
            for (i in 0..steps) {
                val frac = i.toFloat() / steps
                val x = frac * w
                val y = midY + sin(frac * 2f * PI.toFloat() * 2f + wavePhase) * amp
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(VibrantCobalt, EmeraldMint, SunnyAmber)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
