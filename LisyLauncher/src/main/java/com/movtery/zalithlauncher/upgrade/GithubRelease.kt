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

package com.movtery.zalithlauncher.upgrade

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GitHub 原生 Releases API（`GET /repos/{owner}/{repo}/releases`）返回的单个发行版信息。
 * 直接对接仓库的 Release 页面，无需额外维护一个独立的更新信息仓库。
 */
@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("draft")
    val draft: Boolean = false,
    @SerialName("prerelease")
    val prerelease: Boolean = false,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    @SerialName("assets")
    val assets: List<GithubReleaseAsset> = emptyList()
) {
    /** 展示用的版本名称：优先使用 Release 标题，否则回退到 tag */
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: tagName
}

@Serializable
data class GithubReleaseAsset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("size")
    val size: Long = 0L
)
