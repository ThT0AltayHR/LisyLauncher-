/*
 * LisyLauncher — GPLv3
 */

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.path.LibPath
import java.io.File

/**
 * "Modern Giriş Ekranı" paketini (LisyLauncher-intro) belirtilen sürüme kurar.
 *
 * NOT (dürüst sınır): Bu paket FancyMenu ve Drippy Loading Screen modlarının
 * ayar/customization dosyalarıdır — kendi başına bir mod DEĞİLDİR. Bu fonksiyon
 * sadece bu ayar dosyalarını `<oyun>/config/fancymenu/` klasörüne kopyalar.
 * FancyMenu ve Drippy Loading Screen modlarının kendisi bu pakette YOK
 * (bu ortamda internet erişimi olmadığı için indirilemedi) — kullanıcının bu
 * iki modu kendisinin (Modrinth/CurseForge ekranından) kurması gerekiyor.
 */
object FancyMenuIntroInstaller {

    fun isAvailable(): Boolean = LibPath.MENU_INTRO_DIR.exists()

    /** @return true ise kopyalama başarılı oldu */
    fun install(version: Version): Boolean {
        val sourceDir = File(LibPath.MENU_INTRO_DIR, "fancymenu")
        if (!sourceDir.exists()) return false

        val targetDir = File(version.getGameDir(), "config/fancymenu")
        return try {
            sourceDir.copyRecursively(target = targetDir, overwrite = true)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isInstalled(version: Version): Boolean =
        File(version.getGameDir(), "config/fancymenu/lisylauncher_intro_version.txt").exists()
}
