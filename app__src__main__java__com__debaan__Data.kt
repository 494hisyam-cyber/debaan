package com.debaan

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Maulid(val id: String, val title: String, val author: String, val file: String)
data class Verse(val ar: String, val lat: String, val id: String)
data class Section(val title: String, val verses: List<Verse>)

object Catalog {
    val items = listOf(
        Maulid("barzanji", "Maulid al-Barzanji", "Syekh Ja'far al-Barzanji", "barzanji.txt"),
        Maulid("diba", "Maulid ad-Diba'i", "Imam Abdurrahman ad-Diba'i", "diba.txt"),
        Maulid("simthud", "Maulid Simthud Durar", "Al-Habib Ali bin Muhammad al-Habsyi", "simthud.txt"),
        Maulid("dhiya", "Maulid adh-Dhiya'ul Lami'", "Al-Habib Umar bin Hafidz", "dhiya.txt"),
        Maulid("syaraful", "Maulid Syaraful Anam", "Al-Imam Abu Qasim As-Sayyid", "syaraful.txt"),
        Maulid("burdah", "Qasidah Burdah", "Imam al-Bushiri", "burdah.txt"),
    )

    fun byId(id: String?): Maulid? = items.firstOrNull { it.id == id }
}

/** Membaca berkas teks di assets/maulid dan mengubahnya menjadi daftar bab. */
fun loadSections(context: Context, m: Maulid): List<Section> {
    val text = context.assets.open("maulid/${m.file}").bufferedReader(Charsets.UTF_8).use { it.readText() }
    return parseSections(text)
}

private fun blocks(s: String): List<String> =
    s.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() }

fun parseSections(raw: String): List<Section> {
    val text = raw.removePrefix("﻿").replace("\r\n", "\n")
    val out = mutableListOf<Section>()
    var title: String? = null
    val ar = StringBuilder()
    val lt = StringBuilder()
    val idn = StringBuilder()
    var cur: StringBuilder? = null

    fun flush() {
        val t = title
        if (t != null) {
            val a = blocks(ar.toString())
            val l = blocks(lt.toString())
            val d = blocks(idn.toString())
            val n = maxOf(a.size, l.size, d.size)
            val verses = (0 until n).map { i ->
                Verse(a.getOrElse(i) { "" }, l.getOrElse(i) { "" }, d.getOrElse(i) { "" })
            }
            out += Section(t, verses)
        }
        ar.clear(); lt.clear(); idn.clear()
        cur = null
    }

    for (line in text.lines()) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("==") -> {
                flush()
                title = trimmed.removePrefix("==").trim()
            }
            trimmed == "[ar]" -> cur = ar
            trimmed == "[lat]" -> cur = lt
            trimmed == "[id]" -> cur = idn
            trimmed.startsWith("#") -> Unit
            else -> cur?.append(line)?.append('\n')
        }
    }
    flush()
    return out
}

/** Pengaturan tampilan, favorit, dan posisi baca (disimpan di SharedPreferences). */
class Prefs(context: Context, ids: List<String>) {
    private val sp = context.getSharedPreferences("debaan_prefs", Context.MODE_PRIVATE)

    var showArab by mutableStateOf(sp.getBoolean("arab", true))
    var showLatin by mutableStateOf(sp.getBoolean("latin", true))
    var showTerjemah by mutableStateOf(sp.getBoolean("terjemah", true))
    var sizeIndex by mutableIntStateOf(sp.getInt("size", 1).coerceIn(0, 3))
    var lastId by mutableStateOf(sp.getString("last", null))

    val favorites = mutableStateListOf<String>().apply {
        addAll(sp.getStringSet("fav", emptySet()) ?: emptySet())
    }
    private val progress = mutableStateMapOf<String, Float>().apply {
        ids.forEach { put(it, sp.getFloat("p_$it", 0f)) }
    }

    val arabSp: Int get() = arabSizes[sizeIndex]
    val textSp: Int get() = textSizes[sizeIndex]

    fun setArab(v: Boolean) { showArab = v; sp.edit().putBoolean("arab", v).apply() }
    fun setLatin(v: Boolean) { showLatin = v; sp.edit().putBoolean("latin", v).apply() }
    fun setTerjemah(v: Boolean) { showTerjemah = v; sp.edit().putBoolean("terjemah", v).apply() }
    fun setSize(i: Int) { sizeIndex = i; sp.edit().putInt("size", i).apply() }

    fun isFav(id: String) = id in favorites
    fun toggleFav(id: String) {
        if (!favorites.remove(id)) favorites.add(id)
        sp.edit().putStringSet("fav", favorites.toSet()).apply()
    }

    fun progressOf(id: String): Float = progress[id] ?: 0f
    fun position(id: String): Pair<Int, Int> = sp.getInt("i_$id", 0) to sp.getInt("o_$id", 0)

    fun savePosition(id: String, index: Int, offset: Int, p: Float) {
        progress[id] = p
        lastId = id
        sp.edit()
            .putInt("i_$id", index)
            .putInt("o_$id", offset)
            .putFloat("p_$id", p)
            .putString("last", id)
            .apply()
    }

    companion object {
        val sizeLabels = listOf("Kecil", "Sedang", "Besar", "Sangat Besar")
        private val arabSizes = listOf(20, 26, 32, 38)
        private val textSizes = listOf(13, 15, 17, 19)
    }
}
