package com.debaan

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp

/** Satu set warna tampilan: utama, latar, kartu, dan teks. */
class Palette(val name: String, val primary: Long, val bg: Long, val card: Long, val text: Long)

val Palettes = listOf(
    Palette("Hijau Sage", 0xFF5B8A5F, 0xFFEEF2E7, 0xFFF9FBF5, 0xFF1F2B21),
    Palette("Biru Laut", 0xFF3B7CA8, 0xFFE8F0F6, 0xFFF7FAFC, 0xFF1B2631),
    Palette("Ungu Lembut", 0xFF7B5EA7, 0xFFF0EBF6, 0xFFFAF7FD, 0xFF261F30),
    Palette("Cokelat Emas", 0xFFA0702F, 0xFFF5EEE1, 0xFFFDF9F0, 0xFF2E241A),
    Palette("Merah Marun", 0xFF9C4A55, 0xFFF6ECEC, 0xFFFDF8F8, 0xFF2E1D20),
    Palette("Gelap Hijau", 0xFF7FB886, 0xFF121A14, 0xFF1B261E, 0xFFE6EEE4),
    Palette("Hitam Pekat", 0xFF8FC99A, 0xFF000000, 0xFF111111, 0xFFEDEDED),
)

/** Keadaan tampilan (warna & huruf) yang disimpan di SharedPreferences. */
object Look {
    var presetIndex by mutableIntStateOf(0) // -1 = kustom
    var primary by mutableStateOf(Color(Palettes[0].primary))
    var bg by mutableStateOf(Color(Palettes[0].bg))
    var card by mutableStateOf(Color(Palettes[0].card))
    var text by mutableStateOf(Color(Palettes[0].text))
    var fontIndex by mutableIntStateOf(0)

    private fun sp(c: Context) = c.getSharedPreferences("debaan_look", Context.MODE_PRIVATE)

    fun load(c: Context) {
        val s = sp(c)
        fontIndex = s.getInt("font", 0).coerceIn(0, FontOptions.lastIndex)
        val idx = s.getInt("preset", 0)
        if (idx in Palettes.indices) {
            applyPalette(Palettes[idx], idx)
        } else {
            presetIndex = -1
            primary = Color(s.getInt("p", Palettes[0].primary.toInt()))
            bg = Color(s.getInt("b", Palettes[0].bg.toInt()))
            card = Color(s.getInt("c", Palettes[0].card.toInt()))
            text = Color(s.getInt("t", Palettes[0].text.toInt()))
        }
    }

    private fun applyPalette(p: Palette, idx: Int) {
        presetIndex = idx
        primary = Color(p.primary); bg = Color(p.bg); card = Color(p.card); text = Color(p.text)
    }

    fun choosePreset(c: Context, idx: Int) {
        applyPalette(Palettes[idx], idx)
        sp(c).edit().putInt("preset", idx).apply()
    }

    fun setCustom(c: Context, p: Color = primary, b: Color = bg, cd: Color = card, t: Color = text) {
        presetIndex = -1
        primary = p; bg = b; card = cd; text = t
        sp(c).edit().putInt("preset", -1).putInt("p", p.toArgb()).putInt("b", b.toArgb())
            .putInt("c", cd.toArgb()).putInt("t", t.toArgb()).apply()
    }

    fun chooseFont(c: Context, i: Int) {
        fontIndex = i
        sp(c).edit().putInt("font", i).apply()
    }

    val isDark: Boolean get() = bg.luminance() < 0.4f
    val paletteName: String get() = if (presetIndex in Palettes.indices) Palettes[presetIndex].name else "Kustom"
}

// Warna yang dipakai seluruh layar; semuanya mengikuti Look sehingga langsung berubah.
val Sage: Color get() = Look.bg
val CardBg: Color get() = Look.card
val Green: Color get() = Look.primary
val GreenDark: Color get() = if (Look.isDark) lerp(Look.primary, Color.White, 0.2f) else lerp(Look.primary, Color.Black, 0.3f)
val Gold = Color(0xFFE2C26E)
val Ink: Color get() = Look.text
val Muted: Color get() = lerp(Look.text, Look.bg, 0.4f)
val Line: Color get() = lerp(Look.bg, Look.text, 0.12f)

@Composable
fun DebaanTheme(content: @Composable () -> Unit) {
    val onPrimary = if (Look.primary.luminance() > 0.5f) Color.Black else Color.White
    val scheme = if (Look.isDark) {
        darkColorScheme(
            primary = Green, onPrimary = onPrimary, background = Sage, onBackground = Ink,
            surface = CardBg, onSurface = Ink, surfaceVariant = Line, onSurfaceVariant = Muted,
            surfaceContainer = CardBg, surfaceContainerHigh = CardBg,
        )
    } else {
        lightColorScheme(
            primary = Green, onPrimary = onPrimary, background = Sage, onBackground = Ink,
            surface = CardBg, onSurface = Ink, surfaceVariant = Line, onSurfaceVariant = Muted,
            surfaceContainer = CardBg, surfaceContainerHigh = CardBg,
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
