package com.debaan

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cegah mode gelap paksa (mis. Xiaomi) membalik warna; tema gelap diatur sendiri di Pengaturan.
        if (Build.VERSION.SDK_INT >= 29) window.decorView.isForceDarkAllowed = false
        Look.load(applicationContext)
        setContent { DebaanTheme { DebaanApp() } }
    }
}

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Beranda", Icons.Default.Home),
    Tab("Koleksi", Icons.Default.MenuBook),
    Tab("Favorit", Icons.Default.Favorite),
    Tab("Pengaturan", Icons.Default.Settings),
)

@Composable
fun DebaanApp() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(ctx.applicationContext, Catalog.items.map { it.id }) }
    val updater = remember { UpdateController(ctx, scope) }

    val act = ctx as ComponentActivity
    val dark = Look.isDark
    LaunchedEffect(dark) {
        val bar = if (dark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        act.enableEdgeToEdge(statusBarStyle = bar, navigationBarStyle = bar)
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val open = Catalog.byId(openId)

    // Cek pembaruan otomatis saat aplikasi dibuka (diam-diam jika gagal).
    LaunchedEffect(Unit) { updater.check(silent = true) }
    BackHandler(enabled = open != null) { openId = null }

    Scaffold(
        containerColor = Sage,
        bottomBar = {
            if (open == null) {
                // Bilah menu melayang berbentuk pil, ikon terpilih di dalam lingkaran berwarna.
                Box(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 22.dp, end = 22.dp, bottom = 14.dp, top = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(36.dp),
                        color = CardBg,
                        shadowElevation = 10.dp,
                        tonalElevation = 0.dp,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            tabs.forEachIndexed { i, t ->
                                val sel = tab == i
                                Box(
                                    Modifier.size(52.dp).clip(CircleShape)
                                        .background(if (sel) Green else Color.Transparent)
                                        .clickable { tab = i },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        t.icon, contentDescription = t.label,
                                        tint = if (sel) MaterialTheme.colorScheme.onPrimary else Muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            if (open != null) {
                ReaderScreen(open, prefs) { openId = null }
            } else {
                when (tab) {
                    0 -> HomeScreen(prefs) { openId = it.id }
                    1 -> CollectionScreen(prefs) { openId = it.id }
                    2 -> FavoritesScreen(prefs) { openId = it.id }
                    else -> SettingsScreen(prefs, updater)
                }
            }
        }
    }

    UpdateDialog(updater)
}
