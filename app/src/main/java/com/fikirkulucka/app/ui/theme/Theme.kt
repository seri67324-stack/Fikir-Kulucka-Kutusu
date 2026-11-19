package com.fikirkulucka.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PearlBackground = Color(0xFFF4F7FB)
val PureCard = Color(0xFFFFFFFF)
val SoftTintCard = Color(0xFFEBF2FF)
val VibrantCobalt = Color(0xFF2563EB)
val EmeraldMint = Color(0xFF059669)
val SunnyAmber = Color(0xFFD97706)
val CoralRose = Color(0xFFE11D48)
val InkDark = Color(0xFF0F172A)
val SlateMuted = Color(0xFF475569)
val BorderSoft = Color(0xFFE2E8F0)

private val LightPalette = lightColorScheme(
    primary = VibrantCobalt,
    secondary = EmeraldMint,
    tertiary = SunnyAmber,
    background = PearlBackground,
    surface = PureCard,
    surfaceVariant = SoftTintCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = InkDark,
    onSurface = InkDark,
    onSurfaceVariant = SlateMuted
)

@Composable
fun FikirKuluckaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightPalette,
        content = content
    )
}
