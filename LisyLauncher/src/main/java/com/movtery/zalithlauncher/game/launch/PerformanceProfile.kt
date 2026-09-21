/*
 * LisyLauncher
 * Copyright (C) 2026 AltayHR and contributors
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

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GcType
import com.movtery.zalithlauncher.setting.enums.PerformancePreset

/**
 * Bir performans profilinin Minecraft video seçenekleri (options.txt) karşılığı.
 * `null` olan değerler, oyunun mevcut ayarına dokunulmayacağı anlamına gelir.
 */
private class VideoOptions(
    val maxFps: Int,
    val renderDistance: Int,
    val simulationDistance: Int,
    /** 0 = tümü, 1 = azaltılmış, 2 = en az */
    val particles: Int,
    val entityShadows: Boolean,
    val fancyGraphics: Boolean,
    val mipmapLevels: Int? = null,
    val biomeBlend: Int? = null,
    val entityDistance: Float? = null
)

private fun currentVideoOptions(): VideoOptions? = when (AllSettings.perfPreset.getValue()) {
    PerformancePreset.OFF -> null
    PerformancePreset.BATTERY -> VideoOptions(
        maxFps = 60,
        renderDistance = 4,
        simulationDistance = 5,
        particles = 2,
        entityShadows = false,
        fancyGraphics = false,
        mipmapLevels = 0,
        biomeBlend = 0,
        entityDistance = 0.5f
    )
    PerformancePreset.BALANCED -> VideoOptions(
        maxFps = 120,
        renderDistance = 6,
        simulationDistance = 6,
        particles = 1,
        entityShadows = false,
        fancyGraphics = false,
        mipmapLevels = 2,
        biomeBlend = 1,
        entityDistance = 0.75f
    )
    PerformancePreset.MAX_FPS -> VideoOptions(
        maxFps = 260,
        renderDistance = 5,
        simulationDistance = 5,
        particles = 2,
        entityShadows = false,
        fancyGraphics = false,
        mipmapLevels = 0,
        biomeBlend = 0,
        entityDistance = 0.5f
    )
    PerformancePreset.CUSTOM -> VideoOptions(
        maxFps = AllSettings.perfMaxFps.getValue(),
        renderDistance = AllSettings.perfRenderDistance.getValue(),
        simulationDistance = AllSettings.perfSimulationDistance.getValue(),
        particles = if (AllSettings.perfMinimalParticles.getValue()) 2 else 0,
        entityShadows = !AllSettings.perfDisableEntityShadows.getValue(),
        fancyGraphics = !AllSettings.perfFastGraphics.getValue()
    )
}

/**
 * Seçili performans profilini Minecraft seçeneklerine uygular.
 * Profil kapalıysa (OFF) oyunun ayarlarına hiç dokunulmaz.
 * Bilinmeyen anahtarlar Minecraft tarafından sessizce yok sayılır.
 */
fun MCOptions.applyPerformanceProfile() {
    val options = currentVideoOptions() ?: return

    set("maxFps", options.maxFps.toString())
    set("renderDistance", options.renderDistance.toString())
    set("simulationDistance", options.simulationDistance.toString())
    set("particles", options.particles.toString())
    set("entityShadows", options.entityShadows.toString())
    //Minecraft 1.15 ve öncesi
    set("fancyGraphics", options.fancyGraphics.toString())
    //Minecraft 1.16 ve sonrası (0 = hızlı, 1 = detaylı)
    set("graphicsMode", if (options.fancyGraphics) "1" else "0")
    options.mipmapLevels?.let { set("mipmapLevels", it.toString()) }
    options.biomeBlend?.let { set("biomeBlendRadius", it.toString()) }
    options.entityDistance?.let { set("entityDistanceScaling", it.toString()) }
}

/**
 * JVM çöp toplayıcı (GC) ve JIT ayarlarını performans ayarlarına göre uygular.
 * Varsayılan değerler, önceki sürümlerde sabit olarak kullanılan değerlerle aynıdır.
 */
fun MutableList<String>.applyPerformanceJvmArgs() {
    val managedPrefixes = listOf(
        "-XX:+UseG1GC",
        "-XX:+UseParallelGC",
        "-XX:+UseSerialGC",
        "-XX:+UseZGC",
        "-XX:+UseShenandoahGC",
        "-XX:+UseConcMarkSweepGC",
        "-XX:MaxGCPauseMillis",
        "-XX:+ParallelRefProcEnabled",
        "-XX:+DisableExplicitGC",
        "-XX:G1RSetUpdatingPauseTimePercent",
        "-XX:+UseStringDeduplication",
        "-XX:+IgnoreUnrecognizedVMOptions",
        "-XX:TieredStopAtLevel",
        "-XX:MetaspaceSize"
    )
    managedPrefixes.forEach { prefix ->
        removeIf { arg: String -> arg.startsWith(prefix) }
    }

    //Bu sürüm JRE 8/17/21/25'i destekler; bazı -XX seçenekleri eski JVM'lerde tanınmaz.
    //Bu bayrak olmadan tanınmayan seçenek JVM'in hiç açılmamasına yol açar, bu bayrakla yalnızca yok sayılır.
    //Yok saydırdığı seçeneklerden önce eklenmelidir.
    add("-XX:+IgnoreUnrecognizedVMOptions")

    when (AllSettings.perfGcType.getValue()) {
        GcType.G1 -> {
            add("-XX:+UseG1GC")
            add("-XX:MaxGCPauseMillis=${AllSettings.perfGcPauseMillis.getValue()}")
            add("-XX:+ParallelRefProcEnabled")
            add("-XX:G1RSetUpdatingPauseTimePercent=5")
            //Büyük modpack'lerde tekrar eden dizeleri birleştirerek bellek kazandırır
            if (AllSettings.perfStringDedup.getValue()) add("-XX:+UseStringDeduplication")
        }
        GcType.PARALLEL -> add("-XX:+UseParallelGC")
        GcType.SERIAL -> add("-XX:+UseSerialGC")
    }

    //Modların System.gc() ile tüm oyunu dondurmasını engeller
    if (AllSettings.perfDisableExplicitGc.getValue()) add("-XX:+DisableExplicitGC")
    //Yalnızca C1 derleyicisi: ısınma sırasında daha az CPU kullanır
    if (AllSettings.perfLightweightJit.getValue()) add("-XX:TieredStopAtLevel=1")

    //Büyük modpack'lerde sınıf yüklenirken Metaspace'in tekrar tekrar büyümesini önler
    add("-XX:MetaspaceSize=256m")
}
