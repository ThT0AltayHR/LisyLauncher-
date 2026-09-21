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

package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * Oyun başlarken uygulanan performans profili
 *
 * [OFF] dışındaki profiller, Minecraft'ın `options.txt` dosyasındaki video ayarlarını
 * her başlatmada bu profile göre günceller. [CUSTOM] ise kullanıcının kendi değerlerini kullanır.
 */
enum class PerformancePreset(val textRes: Int) {
    OFF(R.string.perf_profile_off),
    BATTERY(R.string.perf_profile_battery),
    BALANCED(R.string.perf_profile_balanced),
    MAX_FPS(R.string.perf_profile_max_fps),
    CUSTOM(R.string.generic_custom)
}

/**
 * JVM çöp toplayıcı (GC) seçimi
 */
enum class GcType(val textRes: Int) {
    G1(R.string.perf_gc_g1),
    PARALLEL(R.string.perf_gc_parallel),
    SERIAL(R.string.perf_gc_serial)
}
