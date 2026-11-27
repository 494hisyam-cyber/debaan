package com.debaan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Rata kanan-kiri ala kitab: tiap syatr dipanjangkan dengan tatwil (ـ) dan jarak kata
// sehingga semua baris dalam satu kolom sama panjang.

private const val TATWEEL = 'ـ'
private val NON_JOIN = "ءآأؤإادذرزوةىٱ".toSet()
private fun isLetter(c: Char) = c in 'ء'..'ي' || c == 'ٱ'
private fun isMark(c: Char) = c in 'ً'..'ٟ' || c == 'ٰ' || c in 'ۖ'..'ۭ'
private fun isAlef(c: Char) = c == 'ا' || c == 'أ' || c == 'إ' || c == 'آ' || c == 'ٱ'

/** Posisi (indeks huruf berikutnya) tempat tatwil boleh disisipkan, urut dari tengah kata. */
private fun slotsOf(w: String): List<Int> {
    val out = mutableListOf<Int>()
    var i = 0
    while (i < w.length) {
        val c = w[i]
        if (isLetter(c)) {
            var j = i + 1
            while (j < w.length && isMark(w[j])) j++
            if (j < w.length && isLetter(w[j]) && c !in NON_JOIN && !(c == 'ل' && isAlef(w[j]))) out += j
            i = j
        } else i++
    }
    val mid = (out.size - 1) / 2f
    return out.sortedBy { kotlin.math.abs(out.indexOf(it) - mid) }
}

private fun withKashida(words: List<String>, count: Int): List<String> {
    if (count <= 0) return words
    val slots = words.map { slotsOf(it) }
    val usable = words.indices.filter { slots[it].isNotEmpty() }
    if (usable.isEmpty()) return words
    val extra = Array(words.size) { mutableMapOf<Int, Int>() }
    var left = count
    var round = 0
    while (left > 0 && round < 80) {
        for (wi in usable) {
            if (left == 0) break
            val sl = slots[wi]
            val pos = sl[round % sl.size]
            extra[wi][pos] = (extra[wi][pos] ?: 0) + 1
            left--
        }
        round++
    }
    return words.mapIndexed { wi, w ->
        if (extra[wi].isEmpty()) w else buildString {
            for (i in w.indices) {
                extra[wi][i]?.let { k -> repeat(k) { append(TATWEEL) } }
                append(w[i])
            }
        }
    }
}

private class Line(val words: List<String>, val gapDp: Float)
private class Laid(val sizeSp: Float, val rows: List<Pair<Line, Line>>)

@Composable
private fun JustLine(line: Line, sizeSp: Float, font: FontFamily, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(line.gapDp.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        line.words.forEach {
            Text(
                it, fontSize = sizeSp.sp, lineHeight = (sizeSp * 1.8f).sp, fontFamily = font, color = Ink,
                softWrap = false, maxLines = 1, style = TextStyle(textDirection = TextDirection.Rtl),
            )
        }
    }
}

/** Sekelompok bait dua kolom (syatr kanan, syatr kiri) dengan tepi lurus di kedua sisi. */
@Composable
fun PairGroup(pairs: List<Pair<String, String>>, base: Float, font: FontFamily) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val midGap = 12.dp
        val colPx = with(density) { ((maxWidth - midGap) / 2).toPx() }
        val colDp = with(density) { colPx.toDp() }
        val laid = remember(pairs, base, colPx, font) {
            val style = TextStyle(fontSize = base.sp, fontFamily = font)
            val minGap = 4f * density.density
            val tw = measurer.measure(TATWEEL.toString(), style).size.width.toFloat().coerceAtLeast(1f)
            val all = pairs.flatMap { listOf(it.first, it.second) }
                .map { t -> t.trim().split(Regex("\\s+")).filter { it.isNotEmpty() } }
            val wb = all.map { ws -> ws.map { measurer.measure(it, style).size.width.toFloat() } }
            var scale = 1f
            all.forEachIndexed { i, ws ->
                val sum = wb[i].sum()
                if (sum > 0f) scale = minOf(scale, (colPx - (ws.size - 1) * minGap) / sum)
            }
            scale = scale.coerceAtLeast(11f / base)
            val lines = all.mapIndexed { i, ws ->
                val n = ws.size
                val sum = wb[i].sum() * scale
                if (n == 1) Line(ws, 0f) else {
                    val room = colPx - sum - (n - 1) * minGap
                    val count = (room * 0.9f / (tw * scale)).toInt().coerceIn(0, n * 6)
                    val gap = ((colPx - sum - count * tw * scale) / (n - 1)).coerceAtLeast(minGap)
                    Line(withKashida(ws, count), gap / density.density)
                }
            }
            Laid(base * scale, lines.chunked(2).map { it[0] to it[1] })
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            laid.rows.forEach { (r, l) ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        JustLine(r, laid.sizeSp, font, Modifier.width(colDp))
                        JustLine(l, laid.sizeSp, font, Modifier.width(colDp))
                    }
                }
            }
        }
    }
}
