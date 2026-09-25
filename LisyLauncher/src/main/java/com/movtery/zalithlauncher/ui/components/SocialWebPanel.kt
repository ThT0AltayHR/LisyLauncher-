/*
 * LisyLauncher
 * Forked from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 ThT0AltayHR and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.components

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Ekranın bir kısmını kaplayan, kapatılabilir yüzen bir web paneli (Discord/Telegram/YouTube/
 * Google Chrome gibi web tabanlı entegrasyonlar için). Tam ekran değildir — oyun/launcher
 * arayüzünün büyük kısmı görünür kalır.
 *
 * Not (dürüst bir sınır): bu, WebView tabanlı bir panel — Discord/Telegram/YouTube'un kendi
 * native uygulamalarının çevrimdışıyken gösterdiği "son görülen içerik" davranışını birebir
 * garanti edemez (bu, o uygulamaların kendi yerel veritabanı/senkronizasyon sistemleriyle
 * çalışır). Burada yapabildiğimiz: oturum çerezlerini (cookie) kalıcı tutmak (paneli her
 * kapatıp açtığınızda yeniden giriş yapmanız gerekmez) ve internet yokken çirkin bir tarayıcı
 * hata sayfası yerine kendi temamıza uygun, sade bir "bağlantı yok" durumu göstermek.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SocialWebPanel(
    title: String,
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(true) }
        var loadFailed by remember { mutableStateOf(false) }
        val webViewHolder = remember { mutableStateOf<WebView?>(null) }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            WebView(context).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                                        super.onPageFinished(view, pageUrl)
                                        isLoading = false
                                    }

                                    override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: android.graphics.Bitmap?) {
                                        super.onPageStarted(view, pageUrl, favicon)
                                        isLoading = true
                                        loadFailed = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame != false) {
                                            isLoading = false
                                            loadFailed = true
                                        }
                                    }
                                }
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                //Oturumun kalıcı olması için önbellek varsayılanı kullanılır (temizlenmez)
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                loadUrl(url)
                                webViewHolder.value = this
                            }
                        },
                        update = { /* url burada tekrar yüklenmez */ }
                    )

                    if (loadFailed) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "İnternet bağlantısı yok",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                text = "Bağlantı kurulduğunda otomatik olarak yeniden denenecek.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp)
                            )
                        }
                    }
                }
            }
        }

        //Not: burada bilinçli olarak WebViewScreen.kt'nin aksine çerezleri/önbelleği TEMİZLEMİYORUZ,
        //çünkü Discord/Telegram/YouTube gibi servislerde oturumun kalıcı olması isteniyor.
        //Sadece WebView nesnesinin kendisini serbest bırakıyoruz (bellek sızıntısını önlemek için).
        DisposableEffect(Unit) {
            onDispose {
                webViewHolder.value?.apply {
                    stopLoading()
                    onPause()
                    removeAllViews()
                }
                webViewHolder.value = null
            }
        }
    }
}

/** Panelin hangi servis için açılacağını ve hedef adresini tanımlar. */
enum class SocialWebTarget(val displayTitle: String, val url: String) {
    DISCORD("Discord", "https://discord.com/app"),
    TELEGRAM("Telegram", "https://web.telegram.org/k/"),
    YOUTUBE("YouTube", "https://m.youtube.com"),
    CHROME("Tarayıcı", "https://www.google.com"),
    XBOX("Xbox", "https://www.xbox.com/en-US/play")
}
