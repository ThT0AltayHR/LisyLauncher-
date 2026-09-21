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

package com.movtery.zalithlauncher.ui.screens.main

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.CrashType
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor

@Composable
fun ErrorScreen(
    crashType: CrashType,
    shareLogs: Boolean = true,
    canUpload: Boolean = false,
    canRestart: Boolean = true,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {},
    onOrientationChanged: (Int) -> Unit = {},
    body: @Composable ColumnScope.() -> Unit
) {
    //获取方向信息，展示两套不同的UI
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ErrorScreenLandscape(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            onRotateClick = {
                onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            },
            body = body
        )
    } else {
        ErrorScreenPortrait(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            onRotateClick = {
                onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            },
            body = body
        )
    }
}

/**
 * 崩溃页面（横屏页面）
 */
@Composable
private fun ErrorScreenLandscape(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    onRotateClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            val text = when (crashType) {
                //在启动器崩溃的时候，显示一个较为严重的标题
                CrashType.LAUNCHER_CRASH -> stringResource(R.string.crash_launcher_title, BuildKeys.LAUNCHER_NAME)
                //游戏运行崩溃了，大概和启动器关系不大，仅展示应用标题
                CrashType.GAME_CRASH -> BuildKeys.LAUNCHER_NAME
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                ErrorTitle(
                    modifier = Modifier.padding(horizontal = 56.dp),
                    text = text
                )
                //旋转竖屏
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    onClick = onRotateClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_fullscreen_exit),
                        contentDescription = null
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor(),
        contentColor = onBackgroundColor(),
        contentWindowInsets = if (AllSettings.launcherFullScreen.state) {
            WindowInsets()
        } else {
            WindowInsets.safeContent.only(WindowInsetsSides.Horizontal)
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ErrorContent(
                modifier = Modifier.weight(7f),
                body = body
            )

            ActionLayout(
                modifier = Modifier.weight(3f),
                crashType = crashType,
                shareLogs = shareLogs,
                canUpload = canUpload,
                canRestart = canRestart,
                onShareLogsClick = onShareLogsClick,
                onUploadClick = onUploadClick,
                onRestartClick = onRestartClick,
                onExitClick = onExitClick
            )
        }
    }
}

/**
 * 崩溃页面（竖屏版本）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreenPortrait(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    onRotateClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    //控制下拉菜单的显示状态
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    //应用标题
                    ErrorTitle(text = BuildKeys.LAUNCHER_NAME)
                },
                actions = {
                    //旋转横屏
                    IconButton(
                        onClick = onRotateClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mobile_rotate_filled),
                            contentDescription = null
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.generic_more)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (canUpload) {
                                DropdownMenuItem(
                                    text = {
                                        MarqueeText(text = stringResource(R.string.crash_link_share_button))
                                    },
                                    onClick = {
                                        showMenu = false
                                        onUploadClick()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_share_logs))
                                },
                                onClick = {
                                    showMenu = false
                                    onShareLogsClick()
                                },
                                enabled = shareLogs
                            )
                            if (canRestart) {
                                DropdownMenuItem(
                                    text = {
                                        MarqueeText(text = stringResource(R.string.crash_restart))
                                    },
                                    onClick = {
                                        showMenu = false
                                        onRestartClick()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_exit))
                                },
                                onClick = {
                                    showMenu = false
                                    onExitClick()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScrollWithBar(rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //崩溃类型
            Text(
                text = stringResource(
                    R.string.crash_type,
                    stringResource(crashType.textRes)
                )
            )

            if (crashType == CrashType.LAUNCHER_CRASH) {
                //仅在启动器崩溃时，才显示这行略显严重的文本
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.crash_launcher_title, BuildKeys.LAUNCHER_NAME)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = body
                )
            } else {
                body()
            }
        }
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = body
        )
    }
}

@Composable
private fun ActionLayout(
    modifier: Modifier = Modifier,
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
    val buttonShape = RoundedCornerShape(14.dp)

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.crash_type,
                        stringResource(crashType.textRes)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)
        ) {
            if (canUpload) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = buttonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    onClick = onUploadClick
                ) {
                    ActionIcon(R.drawable.ic_upload)
                    MarqueeText(text = stringResource(R.string.crash_link_share_button))
                }
            }
            if (shareLogs) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = buttonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    onClick = onShareLogsClick
                ) {
                    ActionIcon(R.drawable.ic_share_filled)
                    MarqueeText(text = stringResource(R.string.crash_share_logs))
                }
            }
            if (canRestart) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = buttonShape,
                    onClick = onRestartClick
                ) {
                    ActionIcon(R.drawable.ic_restart_alt)
                    MarqueeText(text = stringResource(R.string.crash_restart))
                }
            }
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                onClick = onExitClick
            ) {
                ActionIcon(R.drawable.ic_exit_to_app)
                MarqueeText(text = stringResource(R.string.crash_exit))
            }
        }
    }
}

@Composable
private fun RowScope.ActionIcon(
    @DrawableRes iconRes: Int
) {
    Icon(
        modifier = Modifier.size(18.dp),
        painter = painterResource(iconRes),
        contentDescription = null
    )
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
private fun ErrorTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.ic_bug_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun PreviewErrorScreen() {
    MaterialExpressiveTheme {
        ErrorScreen(
            crashType = CrashType.LAUNCHER_CRASH,
            shareLogs = true,
            canUpload = true,
            canRestart = true,
            onShareLogsClick = {},
            onUploadClick = {},
            onRestartClick = {},
            onExitClick = {},
            body = {}
        )
    }
}