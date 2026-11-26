package com.rifsy.debaan

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Sage = Color(0xFFEEF2E7)
val CardBg = Color(0xFFF9FBF5)
val Green = Color(0xFF5B8A5F)
val GreenDark = Color(0xFF3F6B45)
val Gold = Color(0xFFE2C26E)
val Ink = Color(0xFF1F2B21)
val Muted = Color(0xFF6E7E70)
val Line = Color(0xFFE1E8D8)

@Composable
fun DebaanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            onPrimary = Color.White,
            background = Sage,
            onBackground = Ink,
            surface = CardBg,
            onSurface = Ink,
            surfaceVariant = Line,
            onSurfaceVariant = Muted,
        ),
        content = content,
    )
}
