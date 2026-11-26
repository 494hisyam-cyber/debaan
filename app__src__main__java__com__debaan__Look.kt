package com.debaan

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

/** Pilihan huruf Arab. Berkas font diunduh GitHub Actions ke assets/fonts saat build. */
class FontOpt(val name: String, val file: String?)

val FontOptions = listOf(
    FontOpt("Bawaan Sistem", null),
    FontOpt("Amiri", "Amiri-Regular.ttf"),
    FontOpt("Scheherazade New", "ScheherazadeNew-Regular.ttf"),
    FontOpt("Noto Naskh Arabic", "NotoNaskhArabic.ttf"),
    FontOpt("Lateef", "Lateef-Regular.ttf"),
    FontOpt("Noto Kufi Arabic", "NotoKufiArabic.ttf"),
    FontOpt("Cairo", "Cairo.ttf"),
)

private val fontCache = HashMap<Int, FontFamily?>()

/** Mengembalikan FontFamily pilihan, atau null bila berkas font tidak ada di APK. */
fun loadFont(ctx: Context, i: Int): FontFamily? {
    val file = FontOptions.getOrNull(i)?.file ?: return FontFamily.Default
    return fontCache.getOrPut(i) {
        runCatching {
            ctx.assets.open("fonts/$file").close()
            FontFamily(ComposeTypeface(Typeface.createFromAsset(ctx.assets, "fonts/$file")))
        }.getOrNull()
    }
}

/** Huruf yang dipakai untuk teks Arab; jatuh ke huruf bawaan bila font hilang. */
@Composable
fun arabFont(): FontFamily {
    val ctx = LocalContext.current
    return remember(Look.fontIndex) { loadFont(ctx, Look.fontIndex) ?: FontFamily.Default }
}

private const val SAMPLE = "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيمِ"

@Composable
fun FontPicker() {
    val ctx = LocalContext.current
    Column {
        FontOptions.forEachIndexed { i, opt ->
            val fam = remember(i) { loadFont(ctx, i) }
            Row(
                Modifier.fillMaxWidth().clickable { Look.chooseFont(ctx, i) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = Look.fontIndex == i,
                    onClick = { Look.chooseFont(ctx, i) },
                    colors = RadioButtonDefaults.colors(selectedColor = Green),
                )
                Column(Modifier.weight(1f)) {
                    Text(opt.name, fontSize = 13.sp, color = Ink)
                    if (fam == null) {
                        Text("belum tersedia di versi ini", fontSize = 11.sp, color = Muted)
                    } else {
                        Text(
                            SAMPLE, fontFamily = fam, fontSize = 22.sp, color = Ink,
                            style = TextStyle(textDirection = TextDirection.Rtl),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FontDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jenis Huruf Arab") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { FontPicker() } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai") } },
    )
}

// ---------------------------------------------------------------- warna

@Composable
private fun Swatch(p: Palette, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(Color(p.bg))
                .border(if (selected) 3.dp else 1.dp, if (selected) Green else Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(Color(p.primary)))
        }
        Text(p.name, fontSize = 11.sp, color = Ink, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ColorEditor(label: String, color: Color, key: Any, onChange: (Color) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val init = remember(key) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(android.graphics.Color.argb(255, (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt()), it) }
    }
    var h by remember(key) { mutableFloatStateOf(init[0]) }
    var s by remember(key) { mutableFloatStateOf(init[1]) }
    var v by remember(key) { mutableFloatStateOf(init[2]) }
    fun push() = onChange(Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))))

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color).border(1.dp, Line, RoundedCornerShape(8.dp)))
        }
        if (open) {
            val colors = SliderDefaults.colors(thumbColor = Green, activeTrackColor = Green)
            Text("Corak", fontSize = 11.sp, color = Muted)
            Slider(h, { h = it; push() }, valueRange = 0f..360f, colors = colors)
            Text("Kepekatan", fontSize = 11.sp, color = Muted)
            Slider(s, { s = it; push() }, valueRange = 0f..1f, colors = colors)
            Text("Terang", fontSize = 11.sp, color = Muted)
            Slider(v, { v = it; push() }, valueRange = 0f..1f, colors = colors)
        }
    }
}

@Composable
fun ThemeDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Warna Tampilan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Palettes.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { p ->
                            val idx = Palettes.indexOf(p)
                            Swatch(p, Look.presetIndex == idx) { Look.choosePreset(ctx, idx) }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.width(76.dp)) }
                    }
                }
                HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    if (Look.presetIndex == -1) "Warna Kustom (aktif)" else "Warna Kustom — ketuk untuk mengatur",
                    fontSize = 13.sp, color = Muted,
                )
                val key = Look.presetIndex
                ColorEditor("Warna utama", Look.primary, key) { Look.setCustom(ctx, p = it) }
                ColorEditor("Latar belakang", Look.bg, key) { Look.setCustom(ctx, b = it) }
                ColorEditor("Warna kartu", Look.card, key) { Look.setCustom(ctx, cd = it) }
                ColorEditor("Warna teks", Look.text, key) { Look.setCustom(ctx, t = it) }
                Spacer(Modifier.height(4.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai") } },
    )
}
