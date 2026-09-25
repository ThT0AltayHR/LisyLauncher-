/*
 * LisyLauncher
 * Copyright (C) 2025 AltayHR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.ui.screens.splash

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.VideoPlayer

/**
 * Uygulama her açıldığında sırasıyla iki tam ekran yatay video oynatır:
 *   1. splash_pre  — kısa açılış videosu (4 sn, LisyLoad.mp4)
 *   2. splash_intro — ana tanıtım videosu (13 sn, LisyLauncher-Loading.mp4)
 * Her iki video da sesli oynatılır. Herhangi bir video hata verirse bir
 * sonrakine geçer; ikincisi bittiğinde [onFinished] çağrılır.
 */
@Composable
fun SplashVideoScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)

    // 0 = ön video (splash_pre), 1 = ana video (splash_intro), 2 = bitti
    var stage by remember { mutableIntStateOf(0) }
    var globalFinished by remember { mutableStateOf(false) }

    fun advance() {
        when (stage) {
            0 -> stage = 1          // ön video bitti → ana videoya geç
            1 -> {                  // ana video bitti → launcher'a geç
                if (!globalFinished) {
                    globalFinished = true
                    currentOnFinished()
                }
                stage = 2
            }
        }
    }

    val rawIds = remember {
        listOf(R.raw.splash_pre, R.raw.splash_intro)
    }

    if (stage < rawIds.size) {
        val uri = remember(stage) {
            Uri.parse("android.resource://${context.packageName}/${rawIds[stage]}")
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayer(
                videoUri = uri,
                modifier = Modifier.fillMaxSize(),
                autoPlay = true,
                loop = false,
                muted = false,
                onEnded = { advance() },
                onError  = { advance() }   // hata → bir sonrakine geç
            )
        }
    }
}
