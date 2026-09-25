/*
 * LisyLauncher
 * Copyright (C) 2025 AltayHR
 * GPLv3
 */

package com.movtery.zalithlauncher.ui.screens.content.serverlist

import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R

// ──────────────────────────────────────────────────────────────────────────────
// Veri modeli
// ──────────────────────────────────────────────────────────────────────────────

data class ServerSite(
    val name: String,
    val description: String,
    val baseUrl: String,
    /** URL'ye eklenecek arama query parametresi biçimi: "{query}" yerine gerçek terim gelir */
    val searchUrlTemplate: String = baseUrl,
    val gameModes: List<String> = emptyList(),
    val supportsVersionFilter: Boolean = false,
    val category: String = "Genel"
)

val SERVER_SITES = listOf(
    ServerSite("Minecraft Java Servers",  "Kapsamlı Java sunucu listesi",
        "https://minecraft-java-servers.com/", "https://minecraft-java-servers.com/?search={query}",
        listOf("Survival","PvP","SkyBlock","BedWars","Creative"), true, "Liste"),
    ServerSite("ServerBuddy",  "Kullanıcı değerlendirmeli sunucu listesi",
        "https://serverbuddy.net/minecraft-servers/java", "https://serverbuddy.net/minecraft-servers/java?q={query}",
        listOf("Survival","Factions","Prison","Minigames"), true, "Liste"),
    ServerSite("Minecraft Server Radar", "Radarla sunucu bul",
        "https://www.minecraftserverradar.com/servers/java", category = "Liste"),
    ServerSite("MineServerList", "Popülerliğe göre sıralı",
        "https://www.mineserverlist.com/java-servers",
        "https://www.mineserverlist.com/java-servers?search={query}",
        listOf("Survival","SkyBlock","SkyWars","Factions"), false, "Liste"),
    ServerSite("MC Servers Top", "Oyuncu sayısına göre top liste",
        "https://mcservers.top/java/", category = "Top"),
    ServerSite("MineStatus", "Canlı sunucu durumu ve tag filtreleme",
        "https://minestatus.net/server/tag/java", category = "Durum"),
    ServerSite("BestGames MC", "Düzenlenmiş öneriler",
        "https://mc.bestgames.to/java-pc", category = "Öneri"),
    ServerSite("Best Minecraft Server List", "Kullanıcı oylamalı sunucular",
        "https://bestminecraftserverlist.net/servers/java",
        "https://bestminecraftserverlist.net/servers/java?s={query}",
        listOf("Survival","Creative","Hardcore","SkyBlock","Prison"), true, "Liste"),
    ServerSite("Minecraft Best Servers", "Java platform odaklı",
        "https://minecraft-bestservers.com/minecraft-java-servers", category = "Liste"),
    ServerSite("MinecraftBestServers", "Popüler sunucu rehberi",
        "https://minecraftbestservers.com/platform/java/", category = "Liste"),
    ServerSite("TopG", "Geniş kategori seçenekleri",
        "https://topg.org/minecraft-servers/type/Java/",
        "https://topg.org/minecraft-servers/type/Java/?search={query}",
        listOf("Survival","Factions","RPG","SkyBlock","Minigames"), true, "Topluluk"),
    ServerSite("Minecraft Server List", "Düzeltilmiş Java listesi",
        "https://minecraft-serverlist.com/list/java", category = "Liste"),
    ServerSite("GGServerList", "Bedrock & Java, sürüm filtreleme",
        "https://ggserverlist.com/minecraft/tag/bedrock/version/2?edition=java", category = "Liste"),
    ServerSite("MC Servers List – Vanilla", "Vanilla sunucular",
        "https://mc-servers-list.com/category/vanilla/edition/java", listOf("Vanilla"), false, "Oyun Modu"),
    ServerSite("MC Servers List – SMP", "SMP sunucuları",
        "https://mc-servers-list.com/category/smp/edition/java", listOf("SMP"), false, "Oyun Modu"),
    ServerSite("MC Servers List – PvP", "PvP odaklı",
        "https://mc-servers-list.com/category/pvp/edition/java", listOf("PvP"), false, "Oyun Modu"),
    ServerSite("MC Servers List – SkyBlock", "SkyBlock sunucuları",
        "https://mc-servers-list.com/category/skyblock/edition/java", listOf("SkyBlock"), false, "Oyun Modu"),
    ServerSite("MC Servers List – LifeSteal", "LifeSteal sunucuları",
        "https://mc-servers-list.com/category/lifesteal/edition/java", listOf("LifeSteal"), false, "Oyun Modu"),
    ServerSite("MinecraftServersHQ", "Ülkeye göre filtrelenmiş küresel liste",
        "https://minecraftservershq.com", "https://minecraftservershq.com/?search={query}",
        listOf("Survival","Creative","SkyWars","BedWars","Factions"), true, "Küresel")
)

val ALL_GAME_MODES = SERVER_SITES.flatMap { it.gameModes }.distinct().sorted()
val ALL_CATEGORIES = SERVER_SITES.map { it.category }.distinct().sorted()

// ──────────────────────────────────────────────────────────────────────────────
// Ekran
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServerListScreen() {
    var searchQuery    by remember { mutableStateOf("") }
    var selectedMode   by remember { mutableStateOf<String?>(null) }
    var selectedCat    by remember { mutableStateOf<String?>(null) }
    var openedSite     by remember { mutableStateOf<ServerSite?>(null) }
    var webViewUrl     by remember { mutableStateOf("") }

    // Filtre uygulanmış liste
    val filtered = remember(searchQuery, selectedMode, selectedCat) {
        SERVER_SITES.filter { site ->
            val matchesText = searchQuery.isBlank() ||
                site.name.contains(searchQuery, ignoreCase = true) ||
                site.description.contains(searchQuery, ignoreCase = true)
            val matchesMode = selectedMode == null || site.gameModes.contains(selectedMode)
            val matchesCat  = selectedCat  == null || site.category == selectedCat
            matchesText && matchesMode && matchesCat
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Arama çubuğu ──────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.serverlist_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // ── Filtre çipleri ────────────────────────────────────────────────
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Kategori filtresi
            ALL_CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick  = { selectedCat = if (selectedCat == cat) null else cat },
                    label    = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Oyun modu filtresi
            ALL_GAME_MODES.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick  = { selectedMode = if (selectedMode == mode) null else mode },
                    label    = { Text(mode, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Sonuç listesi ─────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.baseUrl }) { site ->
                ServerSiteCard(
                    site = site,
                    searchQuery = searchQuery,
                    onOpen = {
                        openedSite = site
                        webViewUrl = if (searchQuery.isBlank()) site.baseUrl
                                     else site.searchUrlTemplate.replace("{query}", searchQuery)
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // ── WebView dialog ────────────────────────────────────────────────────────
    if (openedSite != null) {
        ServerWebViewDialog(
            url = webViewUrl,
            title = openedSite!!.name,
            onDismiss = { openedSite = null }
        )
    }
}

@Composable
private fun ServerSiteCard(
    site: ServerSite,
    searchQuery: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kategori rozeti
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = site.category.take(2),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(site.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(site.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (site.gameModes.isNotEmpty()) {
                    Text(
                        text = site.gameModes.take(4).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = { onOpen() }) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = stringResource(R.string.serverlist_visit))
            }
        }
    }
}

@Composable
private fun ServerWebViewDialog(
    url: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Başlık çubuğu
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium,
                        color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Kapat", color = Color.White)
                    }
                }

                // WebView
                var loading by remember { mutableStateOf(true) }
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    userAgentString = settings.userAgentString + " LisyLauncher/1.0"
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        loading = false
                                    }
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?, request: WebResourceRequest?
                                    ): Boolean = false
                                }
                                webChromeClient = WebChromeClient()
                                loadUrl(url)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
