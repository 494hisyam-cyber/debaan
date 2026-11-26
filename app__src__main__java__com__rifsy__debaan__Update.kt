package com.rifsy.debaan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateUi {
    data object Idle : UpdateUi
    data object Checking : UpdateUi
    data object UpToDate : UpdateUi
    data class Available(val version: String, val notes: String, val url: String) : UpdateUi
    data class Downloading(val progress: Float) : UpdateUi
    data class Error(val message: String) : UpdateUi
}

private data class ReleaseInfo(val build: Int, val version: String, val notes: String, val apkUrl: String)

/**
 * Memeriksa GitHub Releases terbaru di repo (BuildConfig.GITHUB_REPO),
 * mengunduh APK-nya, lalu membuka pemasang Android.
 */
class UpdateController(private val context: Context, private val scope: CoroutineScope) {

    var state: UpdateUi by mutableStateOf(UpdateUi.Idle)
        private set
    var showDialog: Boolean by mutableStateOf(false)
        private set

    fun dismiss() { showDialog = false }
    fun showAvailable() { showDialog = true }

    fun check(silent: Boolean) {
        if (state is UpdateUi.Checking || state is UpdateUi.Downloading) return
        state = UpdateUi.Checking
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { fetchLatest() } }
            result.onSuccess { info ->
                if (info != null && info.build > BuildConfig.VERSION_CODE) {
                    state = UpdateUi.Available(info.version, info.notes, info.apkUrl)
                    showDialog = true
                } else {
                    state = UpdateUi.UpToDate
                }
            }.onFailure {
                state = if (silent) UpdateUi.Idle
                else UpdateUi.Error("Gagal memeriksa pembaruan. Periksa koneksi internet.")
            }
        }
    }

    fun downloadAndInstall(info: UpdateUi.Available) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            showDialog = false
            state = UpdateUi.Error("Izinkan Debaan memasang aplikasi, lalu tekan Perbarui lagi.")
            val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            runCatching { context.startActivity(i) }
            return
        }
        state = UpdateUi.Downloading(0f)
        showDialog = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { download(info.url) { p -> state = UpdateUi.Downloading(p) } }
            }
            showDialog = false
            result.onSuccess { file ->
                state = info
                install(file)
            }.onFailure {
                state = UpdateUi.Error("Unduhan gagal. Coba lagi.")
            }
        }
    }

    private fun fetchLatest(): ReleaseInfo? {
        val url = URL("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
        val c = url.openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 15_000
        c.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            if (c.responseCode == 404) return null
            val body = c.inputStream.bufferedReader().use { it.readText() }
            val j = JSONObject(body)
            val tag = j.getString("tag_name")
            val build = tag.substringAfterLast('.').toIntOrNull() ?: return null
            val assets = j.getJSONArray("assets")
            var apk: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.getString("name").endsWith(".apk")) {
                    apk = a.getString("browser_download_url")
                    break
                }
            }
            if (apk == null) return null
            val notes = j.optString("body", "").trim().take(400)
            return ReleaseInfo(build, tag.removePrefix("v"), notes, apk)
        } finally {
            c.disconnect()
        }
    }

    private fun download(url: String, onProgress: (Float) -> Unit): File {
        val dir = File(context.cacheDir, "apk").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val out = File(dir, "debaan-update.apk")
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15_000
        c.readTimeout = 30_000
        c.instanceFollowRedirects = true
        try {
            val total = c.contentLengthLong
            var done = 0L
            c.inputStream.use { input ->
                out.outputStream().use { o ->
                    val buf = ByteArray(32 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        o.write(buf, 0, n)
                        done += n
                        if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        } finally {
            c.disconnect()
        }
        return out
    }

    private fun install(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}

fun updateStatusText(s: UpdateUi): String = when (s) {
    UpdateUi.Idle -> "Periksa pembaruan terbaru"
    UpdateUi.Checking -> "Memeriksa…"
    UpdateUi.UpToDate -> "Sudah versi terbaru"
    is UpdateUi.Available -> "Versi ${s.version} tersedia — ketuk untuk unduh"
    is UpdateUi.Downloading -> "Mengunduh…"
    is UpdateUi.Error -> s.message
}

@Composable
fun UpdateDialog(u: UpdateController) {
    if (!u.showDialog) return
    when (val s = u.state) {
        is UpdateUi.Available -> AlertDialog(
            onDismissRequest = { u.dismiss() },
            title = { Text("Pembaruan tersedia") },
            text = {
                Text(
                    "Debaan versi ${s.version} siap diunduh." +
                        if (s.notes.isNotBlank()) "\n\n${s.notes}" else ""
                )
            },
            confirmButton = { TextButton(onClick = { u.downloadAndInstall(s) }) { Text("Unduh & Pasang") } },
            dismissButton = { TextButton(onClick = { u.dismiss() }) { Text("Nanti") } },
        )
        is UpdateUi.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Mengunduh pembaruan…") },
            text = { LinearProgressIndicator(progress = { s.progress }) },
            confirmButton = {},
        )
        else -> Unit
    }
}
