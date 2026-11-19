package com.fikirkulucka.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import com.fikirkulucka.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeReviewCard(
    idea: IdeaEntity,
    onApprove: (IdeaEntity) -> Unit,
    onDiscard: (IdeaEntity) -> Unit,
    onExtendIncubation: (IdeaEntity) -> Unit
) {
    val offsetX = remember(idea.id) { Animatable(0f) }
    val offsetY = remember(idea.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var crossedThreshold by remember(idea.id) { mutableStateOf(false) }

    val ratioX = (offsetX.value / 280f).coerceIn(-1f, 1f)
    val ratioY = (offsetY.value / 240f).coerceIn(-1f, 1f)

    val cardColor = when {
        ratioY < -0.35f -> SoftTintCard
        ratioX > 0.2f -> Color(0xFFECFDF5)
        ratioX < -0.2f -> Color(0xFFFFF1F2)
        else -> PureCard
    }

    val promptText = when {
        ratioY < -0.35f -> "↑ YUKARI BIRAK → +3 GÜN KULUÇKA UZAT"
        ratioX > 0.25f -> "SAĞA BIRAK → ONAYLA & DURUM SEÇ →"
        ratioX < -0.25f -> "← SOLA BIRAK → VAZGEÇ"
        else -> "SAĞA ONAYLA • SOLA VAZGEÇ • YUKARI UZAT"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = (offsetX.value / 28f).coerceIn(-12f, 12f)
            }
            .pointerInput(idea.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetY.value < -220f -> {
                                    offsetY.animateTo(-900f)
                                    onExtendIncubation(idea)
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                offsetX.value > 250f -> {
                                    offsetX.animateTo(1100f)
                                    onApprove(idea)
                                }
                                offsetX.value < -250f -> {
                                    offsetX.animateTo(-1100f)
                                    onDiscard(idea)
                                }
                                else -> {
                                    crossedThreshold = false
                                    launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                    launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val nextX = offsetX.value + dragAmount.x
                            val nextY = offsetY.value + dragAmount.y
                            offsetX.snapTo(nextX)
                            offsetY.snapTo(nextY)
                            if ((abs(nextX) > 250f || nextY < -220f) && !crossedThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                crossedThreshold = true
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        border = BorderStroke(1.2.dp, BorderSoft),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = promptText,
                color = VibrantCobalt,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = idea.content,
                color = InkDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }
    }
}
