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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GcType
import com.movtery.zalithlauncher.setting.enums.PerformancePreset
import com.movtery.zalithlauncher.setting.enums.ResolutionRule
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.EnumSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.viewmodel.EventViewModel

/**
 * Performans (FPS) ayarları: oyun içi video profili, JVM motoru ve ekran seçenekleri
 */
@Composable
fun PerformanceSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Performance, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                PerformanceBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                )
            }

            //Oyun içi video profili
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val custom = AllSettings.perfPreset.state == PerformancePreset.CUSTOM

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = if (custom) CardPosition.Top else CardPosition.Single,
                        unit = AllSettings.perfPreset,
                        title = stringResource(R.string.perf_profile_title),
                        summary = stringResource(R.string.perf_profile_summary),
                        entries = PerformancePreset.entries,
                        getRadioEnable = { true },
                        getRadioText = { preset -> stringResource(preset.textRes) }
                    )

                    if (custom) {
                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.perfMaxFps,
                            title = stringResource(R.string.perf_max_fps_title),
                            summary = stringResource(R.string.perf_max_fps_summary),
                            valueRange = AllSettings.perfMaxFps.floatRange,
                            suffix = " FPS",
                            fineTuningControl = true
                        )

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.perfRenderDistance,
                            title = stringResource(R.string.perf_render_distance_title),
                            summary = stringResource(R.string.perf_render_distance_summary),
                            valueRange = AllSettings.perfRenderDistance.floatRange,
                            fineTuningControl = true
                        )

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.perfSimulationDistance,
                            title = stringResource(R.string.perf_simulation_distance_title),
                            summary = stringResource(R.string.perf_simulation_distance_summary),
                            valueRange = AllSettings.perfSimulationDistance.floatRange,
                            fineTuningControl = true
                        )

                        SwitchSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.perfMinimalParticles,
                            title = stringResource(R.string.perf_particles_title),
                            summary = stringResource(R.string.perf_particles_summary)
                        )

                        SwitchSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.perfDisableEntityShadows,
                            title = stringResource(R.string.perf_entity_shadows_title),
                            summary = stringResource(R.string.perf_entity_shadows_summary)
                        )

                        SwitchSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Bottom,
                            unit = AllSettings.perfFastGraphics,
                            title = stringResource(R.string.perf_fast_graphics_title),
                            summary = stringResource(R.string.perf_fast_graphics_summary)
                        )
                    }
                }
            }

            //JVM motoru
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val isG1 = AllSettings.perfGcType.state == GcType.G1

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.perfGcType,
                        title = stringResource(R.string.perf_gc_title),
                        summary = stringResource(R.string.perf_gc_summary),
                        entries = GcType.entries,
                        getRadioEnable = { true },
                        getRadioText = { gc -> stringResource(gc.textRes) }
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.perfGcPauseMillis,
                        title = stringResource(R.string.perf_gc_pause_title),
                        summary = stringResource(R.string.perf_gc_pause_summary),
                        valueRange = AllSettings.perfGcPauseMillis.floatRange,
                        suffix = " ms",
                        enabled = isG1,
                        fineTuningControl = true
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.perfDisableExplicitGc,
                        title = stringResource(R.string.perf_disable_explicit_gc_title),
                        summary = stringResource(R.string.perf_disable_explicit_gc_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.perfStringDedup,
                        title = stringResource(R.string.perf_string_dedup_title),
                        summary = stringResource(R.string.perf_string_dedup_summary),
                        enabled = isG1
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.perfLightweightJit,
                        title = stringResource(R.string.perf_light_jit_title),
                        summary = stringResource(R.string.perf_light_jit_summary)
                    )
                }
            }

            //Ekran ve oyun içi göstergeler (mevcut ayarlar, burada da erişilebilir)
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val percentResolution = AllSettings.resolutionRule.state == ResolutionRule.PERCENTAGE

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.resolutionRatio,
                        title = stringResource(R.string.settings_renderer_resolution_scale_title),
                        summary = stringResource(R.string.settings_renderer_resolution_scale_summary),
                        valueRange = AllSettings.resolutionRatio.floatRange,
                        suffix = "%",
                        enabled = percentResolution,
                        fineTuningControl = true
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.sustainedPerformance,
                        title = stringResource(R.string.settings_renderer_sustained_performance_title),
                        summary = stringResource(R.string.settings_renderer_sustained_performance_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.showFPS,
                        title = stringResource(R.string.game_menu_option_switch_fps)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.showMemory,
                        title = stringResource(R.string.game_menu_option_switch_memory)
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceBanner(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        scheme.primaryContainer.copy(alpha = 0.6f),
                        scheme.surfaceContainerHigh
                    )
                )
            )
            .border(width = 1.dp, color = scheme.outlineVariant.copy(alpha = 0.6f), shape = shape)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(scheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.ic_gauge_filled),
                contentDescription = null,
                tint = scheme.primary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.perf_banner_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.perf_banner_summary),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
