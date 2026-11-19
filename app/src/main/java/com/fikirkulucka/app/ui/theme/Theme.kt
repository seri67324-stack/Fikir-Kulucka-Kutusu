package com.fikirkulucka.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VoidBlack = Color(0xFF070B14)
val FrostSurface = Color(0xFF111827)
val NeonCyan = Color(0xFF00F5D4)
val WarmAmber = Color(0xFFFFB703)
val DeepIndigo = Color(0xFF4361EE)
val RoseDiscard = Color(0xFFFF4D6D)

private val DarkPalette = darkColorScheme(
    primary = NeonCyan,
    secondary = WarmAmber,
    background = VoidBlack,
    surface = FrostSurface,
    onPrimary = VoidBlack,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun FikirKuluckaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkPalette,
        content = content
    )
}
