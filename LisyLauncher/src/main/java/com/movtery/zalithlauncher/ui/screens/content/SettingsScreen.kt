/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import com.movtery.zalithlauncher.ui.screens.content.settings.AboutInfoScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.ControlManageScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.ControlSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.GameSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.GamepadSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.JavaManageScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.LauncherSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.LauncherSettingsSection
import com.movtery.zalithlauncher.ui.screens.content.settings.PerformanceSettingsScreen
import com.movtery.zalithlauncher.ui.screens.content.settings.RendererSettingsScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun SettingsScreen(
    key: NestedNavKey.Settings,
    backStackViewModel: ScreenBackStackViewModel,
    openLicenseScreen: (raw: Int) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->

        Row(modifier = Modifier.fillMaxSize()) {
            TabMenu(
                modifier = Modifier.fillMaxHeight(),
                isVisible = isVisible,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                navigateTo = { settingKey ->
                    key.backStack.navigateOnce(settingKey)
                }
            )
            NavigationUI(
                key = key,
                mainScreenKey = backStackViewModel.mainScreen.currentKey,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                onCurrentKeyChange = { newKey ->
                    backStackViewModel.settingsScreen.currentKey = newKey
                },
                openLicenseScreen = openLicenseScreen,
                toHomePageEditor = {
                    backStackViewModel.mainScreen.navigateTo(NormalNavKey.HomePageEditor)
                },
                eventViewModel = eventViewModel,
                submitError = submitError,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

private val settingItems = listOf(
    CategoryItem(NormalNavKey.Settings.Performance, { CategoryIcon(R.drawable.ic_gauge_filled, R.string.settings_tab_performance) }, R.string.settings_tab_performance),
    CategoryItem(NormalNavKey.Settings.Renderer, { CategoryIcon(R.drawable.ic_video_settings, R.string.settings_tab_renderer) }, R.string.settings_tab_renderer),
    CategoryItem(NormalNavKey.Settings.Game, { CategoryIcon(R.drawable.ic_rocket_launch_filled, R.string.settings_tab_game) }, R.string.settings_tab_game),
    CategoryItem(NormalNavKey.Settings.Control, { CategoryIcon(R.drawable.ic_videogame_asset_outlined, R.string.settings_tab_control) }, R.string.settings_tab_control),
    CategoryItem(NormalNavKey.Settings.Gamepad, { CategoryIcon(R.drawable.ic_sports_esports_outlined, R.string.settings_tab_gamepad) }, R.string.settings_tab_gamepad),
    CategoryItem(NormalNavKey.Settings.Appearance, { CategoryIcon(R.drawable.ic_palette_filled, R.string.settings_tab_appearance) }, R.string.settings_tab_appearance),
    CategoryItem(NormalNavKey.Settings.Launcher, { CategoryIcon(R.drawable.ic_setting_launcher, R.string.settings_tab_launcher) }, R.string.settings_tab_launcher),
    CategoryItem(NormalNavKey.Settings.JavaManager, { CategoryIcon(R.drawable.ic_coffee_filled, R.string.settings_tab_java_manage) }, R.string.settings_tab_java_manage, division = true),
    CategoryItem(NormalNavKey.Settings.ControlManager, { CategoryIcon(R.drawable.ic_dashboard_outlined, R.string.settings_tab_control_manage) }, R.string.settings_tab_control_manage),
    CategoryItem(NormalNavKey.Settings.AboutInfo, { CategoryIcon(R.drawable.ic_info_outlined, R.string.settings_tab_info_about) }, R.string.settings_tab_info_about, division = true)
)

@Composable
private fun TabMenu(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    settingsScreenKey: TitledNavKey?,
    navigateTo: (TitledNavKey) -> Unit
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fadeEdge(scrollState)
            .width(164.dp)
            .padding(start = 8.dp, end = 4.dp)
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        settingItems.forEach { item ->
            if (item.division) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .alpha(0.4f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            SettingsTabItem(
                selected = settingsScreenKey == item.key,
                icon = item.icon,
                textRes = item.textRes,
                onClick = {
                    navigateTo(item.key)
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsTabItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    textRes: Int,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (selected) scheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        label = "SettingsTabContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) scheme.primary else scheme.onSurfaceVariant,
        label = "SettingsTabContent"
    )

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(containerColor)
                .clickable(role = Role.Tab, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = stringResource(textRes),
                color = contentColor,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Settings,
    mainScreenKey: TitledNavKey?,
    settingsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    openLicenseScreen: (raw: Int) -> Unit,
    toHomePageEditor: () -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.Settings.Performance> {
                    PerformanceSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Renderer> {
                    RendererSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Game> {
                    GameSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Control> {
                    ControlSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.Gamepad> {
                    GamepadSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Appearance> {
                    LauncherSettingsScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        eventViewModel = eventViewModel,
                        toHomePageEditor = toHomePageEditor,
                        submitError = submitError,
                        section = LauncherSettingsSection.Appearance
                    )
                }
                entry<NormalNavKey.Settings.Launcher> {
                    LauncherSettingsScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        eventViewModel = eventViewModel,
                        toHomePageEditor = toHomePageEditor,
                        submitError = submitError,
                        section = LauncherSettingsSection.Launcher
                    )
                }
                entry<NormalNavKey.Settings.JavaManager> {
                    JavaManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.ControlManager> {
                    ControlManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.AboutInfo> {
                    AboutInfoScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        checkUpdate = {
                            eventViewModel.sendEvent(EventViewModel.Event.CheckUpdate)
                        },
                        openLicense = openLicenseScreen,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}