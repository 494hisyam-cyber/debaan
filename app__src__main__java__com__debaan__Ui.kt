package com.debaan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val BUG_EMAIL = "rifqihisyammuddin429@gmail.com"
private const val IG_URL = "https://www.instagram.com/rifsy_am"
private const val FB_URL = "https://www.facebook.com/profile.php?id=61555140984359"

// ---------------------------------------------------------------- komponen umum

@Composable
fun Ornament(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.width(36.dp).height(1.dp).background(Green.copy(alpha = 0.4f)))
        Box(Modifier.padding(horizontal = 6.dp).size(6.dp).clip(CircleShape).background(Green.copy(alpha = 0.6f)))
        Box(Modifier.width(36.dp).height(1.dp).background(Green.copy(alpha = 0.4f)))
    }
}

@Composable
fun ScreenTitle(text: String, prefs: Prefs? = null) {
    Box(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(6.dp))
            Ornament()
        }
        if (prefs != null) ViewModeButton(prefs, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
    }
}

private val ViewModes = listOf(
    "Mendetail" to Icons.AutoMirrored.Filled.ViewList,
    "Ringkas" to Icons.AutoMirrored.Filled.FormatListBulleted,
    "Petak" to Icons.Default.GridView,
)

@Composable
fun ViewModeButton(prefs: Prefs, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { open = true }) {
            Icon(ViewModes[prefs.viewMode].second, "Jenis tampilan", tint = Ink)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = CardBg) {
            Text("Jenis tampilan", fontSize = 13.sp, color = Ink, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                ViewModes.forEachIndexed { i, (label, icon) ->
                    Column(
                        Modifier.clickable { prefs.chooseViewMode(i); open = false }.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(icon, null, tint = Ink, modifier = Modifier.size(30.dp))
                        Box(
                            Modifier.padding(top = 3.dp).width(40.dp).height(3.dp)
                                .background(if (prefs.viewMode == i) Green else Color.Transparent),
                        )
                        Text(label, fontSize = 11.sp, color = Ink, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Cover(modifier: Modifier = Modifier, fontSize: Int = 18) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF4E7F55), Color(0xFF2F5A3A))))
            .border(1.dp, Gold.copy(alpha = 0.7f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text("مولد", color = Gold, fontFamily = FontFamily.Serif, fontSize = fontSize.sp)
    }
}

@Composable
fun MaulidRow(m: Maulid, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Cover(Modifier.size(52.dp, 70.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(m.author, fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(2.dp))
                Text("Teks Arab, Latin & Terjemah", fontSize = 11.sp, color = Muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
fun MaulidCompact(m: Maulid, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                m.title, fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
fun MaulidTile(m: Maulid, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Cover(Modifier.fillMaxWidth().aspectRatio(0.78f), 34)
            Spacer(Modifier.height(8.dp))
            Text(
                m.title, fontFamily = FontFamily.Serif, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = Ink, textAlign = TextAlign.Center, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Text(m.author, fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Daftar maulid sesuai jenis tampilan pilihan (mendetail / ringkas / petak). */
@Composable
fun MaulidCollection(
    items: List<Maulid>,
    prefs: Prefs,
    onOpen: (Maulid) -> Unit,
    header: (LazyGridScope.() -> Unit)? = null,
) {
    val mode = prefs.viewMode
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (mode == 2) 2 else 1),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (mode == 1) 6.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        header?.invoke(this)
        gridItems(items, key = { it.id }) { m ->
            when (mode) {
                0 -> MaulidRow(m) { onOpen(m) }
                1 -> MaulidCompact(m) { onOpen(m) }
                else -> MaulidTile(m) { onOpen(m) }
            }
        }
    }
}

// ---------------------------------------------------------------- beranda

@Composable
fun HomeScreen(prefs: Prefs, onOpen: (Maulid) -> Unit) {
    val last = Catalog.byId(prefs.lastId)
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Debaan", prefs)
        MaulidCollection(Catalog.items, prefs, onOpen) {
            if (last != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        SectionHeader("Teruskan Membaca")
                        Spacer(Modifier.height(8.dp))
                        ContinueCard(last, prefs.progressOf(last.id)) { onOpen(last) }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Koleksi Maulid") }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontFamily = FontFamily.Serif,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ink,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun ContinueCard(m: Maulid, progress: Float, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Cover(Modifier.size(56.dp, 76.dp), 20)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text("Terakhir dibaca", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = Green,
                    trackColor = Line,
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Muted)
        }
    }
}

// ---------------------------------------------------------------- koleksi & favorit

@Composable
fun CollectionScreen(prefs: Prefs, onOpen: (Maulid) -> Unit) {
    var query by remember { mutableStateOf("") }
    val shown = Catalog.items.filter {
        query.isBlank() || it.title.contains(query, true) || it.author.contains(query, true)
    }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Koleksi Maulid", prefs)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari maulid...", color = Muted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = Green,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Green,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        MaulidCollection(shown, prefs, onOpen)
    }
}

@Composable
fun FavoritesScreen(prefs: Prefs, onOpen: (Maulid) -> Unit) {
    val favs = Catalog.items.filter { prefs.isFav(it.id) }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Favorit", prefs)
        if (favs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    "Belum ada favorit.\nKetuk ikon hati di halaman bacaan untuk menambahkan.",
                    color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center,
                )
            }
        } else {
            MaulidCollection(favs, prefs, onOpen)
        }
    }
}

// ---------------------------------------------------------------- pembaca

private sealed interface ReadItem {
    data class Head(val title: String) : ReadItem
    data class V(val verse: Verse) : ReadItem
}

@Composable
fun ReaderScreen(m: Maulid, prefs: Prefs, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val sections by produceState<List<Section>?>(initialValue = null, m.id) {
        value = withContext(Dispatchers.IO) {
            runCatching { loadSections(ctx, m) }.getOrDefault(emptyList())
        }
    }
    var showOptions by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Ink) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    m.title, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium,
                    color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(m.author, fontSize = 12.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { prefs.toggleFav(m.id) }) {
                Icon(
                    if (prefs.isFav(m.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Favorit", tint = Green,
                )
            }
            IconButton(onClick = { showOptions = true }) { Icon(Icons.Default.Tune, "Tampilan teks", tint = Ink) }
        }

        val secs = sections
        when {
            secs == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Memuat…", color = Muted)
            }
            secs.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Teks belum tersedia.", color = Muted)
            }
            else -> ReaderList(m, secs, prefs)
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Tampilan Teks") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Ukuran Teks", fontSize = 13.sp, color = Muted)
                    SizePicker(prefs)
                    HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 8.dp))
                    Text("Jenis Huruf Arab", fontSize = 13.sp, color = Muted)
                    FontPicker()
                    HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 8.dp))
                    SwitchRow("Teks Arab", prefs.showArab) { prefs.setArab(it) }
                    SwitchRow("Transliterasi (Latin)", prefs.showLatin) { prefs.setLatin(it) }
                    SwitchRow("Terjemah", prefs.showTerjemah) { prefs.setTerjemah(it) }
                }
            },
            confirmButton = { TextButton(onClick = { showOptions = false }) { Text("Selesai") } },
        )
    }
}

@Composable
private fun ReaderList(m: Maulid, sections: List<Section>, prefs: Prefs) {
    val rows = remember(sections) {
        buildList<ReadItem> {
            sections.forEach { s ->
                add(ReadItem.Head(s.title))
                var pend = mutableListOf<String>()
                fun flushPairs() {
                    if (pend.isNotEmpty()) add(ReadItem.V(Verse(pend.joinToString("\n"), "", "")))
                    pend = mutableListOf()
                }
                s.verses.forEach { v ->
                    val onlyPairs = v.lat.isBlank() && v.id.isBlank() && v.ar.isNotBlank() &&
                        v.ar.lines().filter { it.isNotBlank() }.all { PAIR in it }
                    if (onlyPairs) pend.addAll(v.ar.lines().filter { it.isNotBlank() }.map { it.trim() })
                    else { flushPairs(); add(ReadItem.V(v)) }
                }
                flushPairs()
            }
        }
    }
    val (i0, o0) = remember(m.id) { prefs.position(m.id) }
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = i0.coerceIn(0, (rows.size - 1).coerceAtLeast(0)),
        initialFirstVisibleItemScrollOffset = o0,
    )

    LaunchedEffect(state, rows.size) {
        snapshotFlow {
            Triple(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset, state.canScrollForward)
        }.collect { (i, o, more) ->
            val p = when {
                !more -> 1f
                rows.size > 1 -> i.toFloat() / (rows.size - 1)
                else -> 0f
            }
            prefs.savePosition(m.id, i, o, p)
        }
    }

    val arabSp = prefs.arabSp
    val arab = arabFont()
    val textSp = prefs.textSp

    Surface(
        shape = RoundedCornerShape(20.dp), color = CardBg,
        modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
    ) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(rows) { item ->
            when (item) {
                is ReadItem.Head -> Column(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        item.title, fontFamily = FontFamily.Serif, fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(Ink.copy(alpha = 0.7f)))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.copy(alpha = 0.7f)))
                }
                is ReadItem.V -> if (item.verse.lat.isBlank() && item.verse.id.isBlank() && isLabel(item.verse.ar)) {
                    Text(
                        item.verse.ar.trim(), fontSize = (arabSp * 0.7f).sp, fontFamily = arab, color = Muted,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    val v = item.verse
                    run {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (prefs.showArab && v.ar.isNotBlank()) {
                                ArabLines(v.ar, arabSp, arab)
                            }
                            if (prefs.showLatin && v.lat.isNotBlank()) {
                                Text(
                                    v.lat,
                                    fontSize = textSp.sp,
                                    lineHeight = (textSp * 1.5f).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = GreenDark,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            if (prefs.showTerjemah && v.id.isNotBlank()) {
                                Text(
                                    v.id,
                                    fontSize = textSp.sp,
                                    lineHeight = (textSp * 1.5f).sp,
                                    color = Muted,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            if (!v.ar.contains(PAIR)) {
                                Box(Modifier.padding(top = 2.dp).width(48.dp).height(1.dp).background(Line))
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

private const val PAIR = " * "

/** Penanda singkat seperti "(اللازمة)" tampil sebagai label kecil tanpa kartu. */
private fun isLabel(t: String): Boolean {
    val x = t.trim()
    return x.length <= 24 && x.startsWith("(") && x.endsWith(")") && !x.contains('\n')
}

/** Teks Arab: baris berpasangan ("syatr1 * syatr2") tampil dua kolom bergaris tengah, baris lain di tengah. */
@Composable
private fun ArabLines(text: String, size: Int, font: FontFamily) {
    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        var i = 0
        while (i < lines.size) {
            if (PAIR in lines[i]) {
                val grp = mutableListOf<Pair<String, String>>()
                while (i < lines.size && PAIR in lines[i]) {
                    val (a, b) = lines[i].split(PAIR, limit = 2)
                    grp += a.trim() to b.trim()
                    i++
                }
                PairGroup(grp, size.toFloat(), font)
            } else {
                if (i > 0) Spacer(Modifier.height(4.dp))
                Text(
                    lines[i], fontSize = size.sp, lineHeight = (size * 1.9f).sp, fontFamily = font,
                    textAlign = TextAlign.Center, color = Ink,
                    style = TextStyle(textDirection = TextDirection.Rtl), modifier = Modifier.fillMaxWidth(),
                )
                i++
            }
        }
    }
}

// ---------------------------------------------------------------- pengaturan

@Composable
fun SizePicker(prefs: Prefs) {
    Column {
        Prefs.sizeLabels.forEachIndexed { i, label ->
            Row(
                Modifier.fillMaxWidth().clickable { prefs.setSize(i) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = prefs.sizeIndex == i,
                    onClick = { prefs.setSize(i) },
                    colors = RadioButtonDefaults.colors(selectedColor = Green),
                )
                Text(label, color = Ink)
            }
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text, color = Green, fontSize = 12.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 4.dp, top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingRow(
    label: String,
    value: String? = null,
    subtitle: String? = null,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = Ink)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Muted)
        }
        if (value != null) Text(value, fontSize = 13.sp, color = Muted)
        if (chevron) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = Ink, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Green, checkedThumbColor = Color.White),
        )
    }
}

@Composable
fun SettingsScreen(prefs: Prefs, updater: UpdateController) {
    val ctx = LocalContext.current
    var showSize by remember { mutableStateOf(false) }
    var showFont by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }
    val status = updater.state

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Pengaturan")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 16.dp),
        ) {
            GroupTitle("Tampilan")
            GroupCard {
                SettingRow("Ukuran Teks", value = Prefs.sizeLabels[prefs.sizeIndex], chevron = true) { showSize = true }
                RowDivider()
                SettingRow("Jenis Huruf Arab", value = FontOptions[Look.fontIndex].name, chevron = true) { showFont = true }
                RowDivider()
                SettingRow("Warna Tampilan", value = Look.paletteName, chevron = true) { showTheme = true }
                RowDivider()
                SwitchRow("Teks Arab", prefs.showArab) { prefs.setArab(it) }
                RowDivider()
                SwitchRow("Transliterasi (Latin)", prefs.showLatin) { prefs.setLatin(it) }
                RowDivider()
                SwitchRow("Terjemah", prefs.showTerjemah) { prefs.setTerjemah(it) }
            }

            GroupTitle("Aplikasi")
            GroupCard {
                SettingRow("Versi Aplikasi", value = BuildConfig.VERSION_NAME)
                RowDivider()
                SettingRow(
                    "Perbarui Aplikasi",
                    subtitle = updateStatusText(status),
                    chevron = status !is UpdateUi.Checking,
                ) {
                    if (status is UpdateUi.Available) updater.showAvailable() else updater.check(silent = false)
                }
            }

            GroupTitle("Kontak")
            GroupCard {
                SettingRow("Laporan Bug", subtitle = "Kirim lewat Gmail", chevron = true) { sendBugReport(ctx) }
                RowDivider()
                SettingRow("Instagram", value = "@rifsy_am", chevron = true) { openUrl(ctx, IG_URL) }
                RowDivider()
                SettingRow("Facebook", value = "El Mahmud Rifqi", chevron = true) { openUrl(ctx, FB_URL) }
            }
        }
    }

    if (showFont) FontDialog { showFont = false }
    if (showTheme) ThemeDialog { showTheme = false }
    if (showSize) {
        AlertDialog(
            onDismissRequest = { showSize = false },
            title = { Text("Ukuran Teks") },
            text = { SizePicker(prefs) },
            confirmButton = { TextButton(onClick = { showSize = false }) { Text("Selesai") } },
        )
    }
}

// ---------------------------------------------------------------- aksi tautan

private fun openUrl(ctx: Context, url: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(ctx, "Tidak ada aplikasi untuk membuka tautan", Toast.LENGTH_SHORT).show()
    }
}

private fun sendBugReport(ctx: Context) {
    val body = "Jelaskan bug yang kamu temukan:\n\n\n---\n" +
        "Versi: ${BuildConfig.VERSION_NAME}\n" +
        "Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n" +
        "Perangkat: ${Build.MANUFACTURER} ${Build.MODEL}"
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(BUG_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Bug Debaan ${BuildConfig.VERSION_NAME}")
        putExtra(Intent.EXTRA_TEXT, body)
        setPackage("com.google.android.gm")
    }
    try {
        ctx.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        intent.setPackage(null)
        try {
            ctx.startActivity(intent)
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(ctx, "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}
