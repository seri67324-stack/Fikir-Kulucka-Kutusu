package com.fikirkulucka.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fikirkulucka.app.data.IdeaEntity
import com.fikirkulucka.app.ui.theme.NeonCyan
import com.fikirkulucka.app.ui.theme.RoseDiscard
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeReviewCard(
    idea: IdeaEntity,
    onApprove: (IdeaEntity) -> Unit,
    onDiscard: (IdeaEntity) -> Unit
) {
    val offsetX = remember(idea.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var crossedThreshold by remember(idea.id) { mutableStateOf(false) }

    val ratio = (offsetX.value / 320f).coerceIn(-1f, 1f)
    val cardColor = when {
        ratio > 0.15f -> NeonCyan.copy(alpha = 0.18f + abs(ratio) * 0.25f)
        ratio < -0.15f -> RoseDiscard.copy(alpha = 0.18f + abs(ratio) * 0.25f)
        else -> Color(0xFF172136)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = (offsetX.value / 26f).coerceIn(-14f, 14f)
            }
            .pointerInput(idea.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > 260f -> {
                                    offsetX.animateTo(1200f)
                                    onApprove(idea)
                                }
                                offsetX.value < -260f -> {
                                    offsetX.animateTo(-1200f)
                                    onDiscard(idea)
                                }
                                else -> {
                                    crossedThreshold = false
                                    offsetX.animateTo(
                                        0f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val updated = offsetX.value + dragAmount.x
                            offsetX.snapTo(updated)
                            if (abs(updated) > 260f && !crossedThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                crossedThreshold = true
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(30.dp),
        color = cardColor,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    ratio > 0.25f -> "SAĞA BIRAK → YAPMAYA DEĞER"
                    ratio < -0.25f -> "SOLA BIRAK → VAZGEÇ"
                    else -> "BU FİKRİ GERÇEKTEN YAPMALI MIYIM?"
                },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = idea.content,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 29.sp
            )
        }
    }
}
