/*
 * LisyLauncher
 * Copyright (C) 2025 AltayHR — GPLv3
 */

package com.movtery.zalithlauncher.ui.screens.content

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.isSlimModel
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.ui.components.PlayerSkin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ─── Veri modeli ─────────────────────────────────────────────────────────────

data class BuiltinSkin(val file: File) {
    val name: String get() = file.nameWithoutExtension
}

// ─── Ekran ───────────────────────────────────────────────────────────────────

@Composable
fun SkinWardrobeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    val currentAccount by AccountsManager.currentAccountFlow.collectAsState()

    // Skin listesi — BUILTIN_SKINS_DIR henüz açılmamış olabilir
    var skins by remember { mutableStateOf<List<BuiltinSkin>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedSkin by remember { mutableStateOf<BuiltinSkin?>(null) }
    var applyLoading by remember { mutableStateOf(false) }

    // PlayerSkin WebView — 3D önizleme için
    val playerSkin = remember(context) { PlayerSkin(context) }

    LaunchedEffect(Unit) {
        loading = true
        skins = loadBuiltinSkins()
        loading = false
    }

    // Seçim değişince 3D önizlemeyi güncelle
    LaunchedEffect(selectedSkin) {
        selectedSkin?.let { skin ->
            withContext(Dispatchers.IO) {
                skin.file.inputStream().use { stream ->
                    playerSkin.loadSkin(inputStream = stream, model = SkinModelType.NONE)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ── Sol: 3D önizleme + Uygula butonu ──────────────────────────
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Önizleme",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A2E))
                ) {
                    if (selectedSkin != null) {
                        AndroidView(
                            factory = { ctx -> playerSkin.loadWebView(ctx) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Bir skin seçin",
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val skin = selectedSkin ?: return@Button
                        val account = currentAccount ?: return@Button
                        scope.launch {
                            applyLoading = true
                            try {
                                withContext(Dispatchers.IO) {
                                    val dest = account.getSkinFile()
                                    dest.parentFile?.mkdirs()
                                    skin.file.copyTo(dest, overwrite = true)
                                    // slim/classic model detect
                                    val slim = skin.file.isSlimModel()
                                    val model = if (slim) SkinModelType.ALEX else SkinModelType.STEVE
                                    AccountsManager.saveAccount(account.copy(skinModelType = model))
                                }
                                snackbarHost.showSnackbar("Skin uygulandı ✓")
                            } catch (e: Exception) {
                                snackbarHost.showSnackbar("Hata: ${e.message}")
                            } finally {
                                applyLoading = false
                            }
                        }
                    },
                    enabled = selectedSkin != null && currentAccount != null && !applyLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (applyLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.wardrobe_apply))
                    }
                }

                if (currentAccount == null) {
                    Text(
                        text = "Hesap seçilmedi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ── Sağ: Skin ızgarası ──────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when {
                    loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    skins.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.wardrobe_loading),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Skinler ilk açılışta yüklenir. Lütfen bekleyin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 72.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(skins, key = { it.file.absolutePath }) { skin ->
                                SkinTile(
                                    skin = skin,
                                    isSelected = skin == selectedSkin,
                                    onClick = { selectedSkin = skin }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SkinTile(skin: BuiltinSkin, isSelected: Boolean, onClick: () -> Unit) {
    val bitmap = remember(skin.file) {
        runCatching {
            // Skin head crop (pixels 8-15 x 8-15 on the 64x64 texture)
            val full = BitmapFactory.decodeFile(skin.file.absolutePath)
            if (full != null) {
                val scale = full.width / 64f
                val x = (8 * scale).toInt()
                val y = (8 * scale).toInt()
                val s = (8 * scale).toInt().coerceAtLeast(1)
                android.graphics.Bitmap.createBitmap(full, x, y, s, s).also { full.recycle() }
            } else null
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2A2A3E))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = skin.name,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            Text("?", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            )
        }
    }
}

// ─── Yardımcı ────────────────────────────────────────────────────────────────

private suspend fun loadBuiltinSkins(): List<BuiltinSkin> = withContext(Dispatchers.IO) {
    val dir = LibPath.BUILTIN_SKINS_DIR
    if (!dir.exists()) return@withContext emptyList()

    // Zip henüz açılmamışsa aç
    val zipFile = File(dir, "builtin_skins.zip")
    val skinsSubdir = File(dir, "Minecraft-Skins-master")
    if (zipFile.exists() && !skinsSubdir.exists()) {
        try {
            java.util.zip.ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".png")) {
                        val outFile = File(dir, entry.name)
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { out -> zis.copyTo(out) }
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (_: Exception) { }
    }

    // Tüm png dosyalarını listele
    dir.walkTopDown()
        .filter { it.isFile && it.extension.lowercase() == "png" }
        .map { BuiltinSkin(it) }
        .sortedBy { it.name }
        .toList()
}
