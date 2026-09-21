<div align="center">

<img src="docs/assets/lisylauncher-logo.png" alt="LisyLauncher" width="760">

**Android için modern, hızlı ve dokunmatik öncelikli Minecraft: Java Edition başlatıcısı**

[![Lisans](https://img.shields.io/github/license/ThT0AltayHR/L-syLauncher?style=for-the-badge&color=blue)](LICENSE)
[![Son Sürüm](https://img.shields.io/github/v/release/ThT0AltayHR/L-syLauncher?include_prereleases&style=for-the-badge&color=success)](https://github.com/ThT0AltayHR/LisyLauncher--/releases)
[![Derleme](https://img.shields.io/github/actions/workflow/status/ThT0AltayHR/L-syLauncher/push_ci.yml?style=for-the-badge&label=DERLEME)](https://github.com/ThT0AltayHR/LisyLauncher--/actions)
[![İndirmeler](https://img.shields.io/github/downloads/ThT0AltayHR/L-syLauncher/total?style=for-the-badge&color=orange)](https://github.com/ThT0AltayHR/LisyLauncher--/releases)
[![Yıldızlar](https://img.shields.io/github/stars/ThT0AltayHR/L-syLauncher?style=for-the-badge&color=yellow)](https://github.com/ThT0AltayHR/LisyLauncher--/stargazers)

![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-Java%20Edition-62B47A?style=flat-square&logo=minecraft&logoColor=white)

</div>

---

## Tanıtım

<div align="center">

<video src="docs/assets/lisylauncher-intro.mp4" poster="docs/assets/intro-poster.jpg" controls muted playsinline width="720">
  <a href="docs/assets/lisylauncher-intro.mp4">
    <img src="docs/assets/intro-poster.jpg" alt="LisyLauncher tanıtım videosu (izlemek için tıklayın)" width="720">
  </a>
</video>

<sub>Video oynatılmıyorsa <a href="docs/assets/lisylauncher-intro.mp4">buradan izleyebilirsiniz</a>.</sub>

</div>

---

## İçindekiler

- [Özellikler](#özellikler)
- [Kurulum](#kurulum)
- [Derleme](#derleme)
- [Otomatik derleme ve yayınlama](#otomatik-derleme-ve-yayınlama)
- [Lisans](#lisans)
- [Geliştirici](#geliştirici)
- [Teşekkürler](#teşekkürler)

---

## Özellikler

### Oynanış
- **Java Edition doğrudan Android'de**: Java çalışma zamanı (JRE 8/17/21/25) otomatik yönetilir; modlu veya vanilla Minecraft telefon ve tablette çalışır.
- **Dokunmatik öncelikli kontroller**: 4 yönlü D-pad ve tam dokunmatik etkileşim. Dokunmak saldırı, basılı tutmak yerleştirme/etkileşimdir.
- **Kontrol düzenleyici ve oyun kumandası desteği**: Kendi kontrol düzeninizi sürükle-bırak ile tasarlayın.
- **Çoklu render motoru**: OpenGL ES, Vulkan (deneysel), ANGLE ve farklı GL4ES varyantları.
- **Çok oyunculu**: Terracotta ile oda tabanlı çok oyunculu bağlantı.

### Modlar ve içerik
- **Forge, Fabric, NeoForge, Quilt ve OptiFine** kurulum desteği.
- **CurseForge ve Modrinth** üzerinden mod ve modpack indirme.
- Mikrofon kullanan modlar için otomatik izin akışı.

### Performans (FPS)
- **Performans profilleri**: Pil tasarrufu, Dengeli, Maksimum FPS ve Özel. Seçilen profil her başlatmada oyunun video seçeneklerine (FPS sınırı, görüş ve simülasyon mesafesi, parçacıklar, gölgeler, grafik kalitesi) uygulanır.
- **JVM motoru ayarları**: Çöp toplayıcı seçimi (G1, Parallel, Serial), duraklama hedefi, `System.gc()` çağrılarını yok sayma, dize tekilleştirme ve hafif JIT modu.
- **Ekran ayarları**: Çözünürlük ölçeği, sürekli performans modu, oyun içi FPS ve bellek göstergesi.

### Arayüz
- **Yeni yerleşim**: Sol kenar çubuğu ile gezinme, marka başlıklı ana sayfa, hızlı erişim kutucukları ve kategorilere ayrılmış ayarlar (Performans, Oyun, Kontrol, Görünüm, Başlatıcı ve daha fazlası).
- **Özel SVG simge seti**: Tüm simgeler vektör (SVG) tabanlıdır, emoji kullanılmaz. Kaynak dosyalar [`design/icons`](design/icons) klasöründedir.
- **Astra teması**: Koyu, kömür rengi yüzeyler ve zümrüt yeşili vurgu; açık tema ve özel renk seçenekleri de mevcuttur.
- **Modern hata ekranı**: Çökme durumunda günlük paylaşma, yeniden başlatma ve çıkış işlemleri tek ekranda.
- **17 dil desteği**: Türkçe, English, Español, Português (PT/BR), Italiano, Русский, 日本語, 한국어, 简体中文, 繁體中文, Tiếng Việt, ไทย, Bahasa Indonesia, Filipino, العربية, ئۇيغۇرچە.

### Hesaplar ve araçlar
- Orijinal (Microsoft) hesap ve çevrimdışı hesap yan yana.
- Skin görüntüleyici, dosya yöneticisi, günlük görüntüleyici.
- Güncelleme takibi: Bu depodaki GitHub sürümlerini izler; kararlı/beta filtreleme ve sürüm geçmişi.

---

## Kurulum

En güncel sürümü [**Releases**](https://github.com/ThT0AltayHR/LisyLauncher--/releases/latest) sayfasından indirin.

Cihazınızın işlemci mimarisine uygun APK'yı seçin. Emin değilseniz `arm64` sürümü modern telefonların neredeyse tamamında çalışır:

| Dosya | Mimari |
|---|---|
| `LisyLauncher-X.X.X-arm64.apk` | 64-bit ARM (çoğu modern telefon) |
| `LisyLauncher-X.X.X-arm.apk` | 32-bit ARM (eski cihazlar) |
| `LisyLauncher-X.X.X-x86_64.apk` | 64-bit Intel/AMD (bazı tabletler, emülatörler) |
| `LisyLauncher-X.X.X.apk` | Evrensel (tüm mimariler, daha büyük dosya) |

---

## Derleme

```bash
git clone https://github.com/ThT0AltayHR/LisyLauncher--.git
cd L-syLauncher
./gradlew LisyLauncher:assembleRelease
```

**Gereksinimler**
- Android Studio (güncel sürüm önerilir)
- Android SDK, en düşük API 26
- JDK 21

---

## Otomatik derleme ve yayınlama

Depo, GitHub Actions ile tam otomatik derleme ve yayınlama kullanır. Ek bir kurulum gerekmez.

1. **Derleme (`build.yml`)**: Her push'ta 5 mimari (`all`, `arm`, `arm64`, `x86`, `x86_64`) için ayrı ayrı APK derlenir.
2. **İmzalama**: Release APK'ları otomatik olarak imzalanır.
3. **Yayınlama (`release_ci.yml`)**: Bir GitHub sürümü yayınlandığında tüm mimarilerin APK'ları toplanır ve doğrudan o sürüme eklenir.

---

## Lisans

LisyLauncher, **GNU Genel Kamu Lisansı sürüm 3 (GPLv3)** ile lisanslanmıştır. Lisansın tam metni [LICENSE](LICENSE) dosyasındadır.

```text
LisyLauncher
Copyright (C) 2026 AltayHR ve katkıda bulunanlar

Bu program özgür yazılımdır: Free Software Foundation tarafından yayımlanan
GNU Genel Kamu Lisansı'nın 3. sürümünün ya da (tercihinize bağlı olarak)
daha sonraki bir sürümünün koşulları altında yeniden dağıtabilir ve/veya
değiştirebilirsiniz.

Bu program yararlı olması umuduyla dağıtılmaktadır, ancak HİÇBİR GARANTİ
VERİLMEMEKTEDİR; satılabilirlik ya da belirli bir amaca uygunluk garantisi
dahil hiçbir zımni garanti sunulmaz. Ayrıntılar için GNU Genel Kamu
Lisansı'na bakın.

Bu programla birlikte GNU Genel Kamu Lisansı'nın bir kopyasını almış
olmalısınız. Almadıysanız <https://www.gnu.org/licenses/gpl-3.0.txt>
adresine bakın.
```

**Kaynak ve atıflar.** LisyLauncher, GPLv3 ile lisanslı **[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)** (© MovTery ve katkıda bulunanlar) temel alınarak geliştirilmiştir; başlatma altyapısı PojavLauncher'a dayanır. Bu projelerin telif hakkı bildirimleri ve lisans metinleri korunmuştur. LisyLauncher, orijinal ZalithLauncher2 projesiyle resmi bir bağlantıya sahip değildir.

---

## Geliştirici

- **AltayHR** ([@ThT0AltayHR](https://github.com/ThT0AltayHR)): LisyLauncher ana geliştiricisi
- Katkıda bulunanlar: [GitHub katkıda bulunanlar sayfası](https://github.com/ThT0AltayHR/LisyLauncher--/graphs/contributors)

---

## Teşekkürler

- **[MovTery](https://github.com/MovTery)** ve [ZalithLauncher2 katkıda bulunanları](https://github.com/ZalithLauncher/ZalithLauncher2/graphs/contributors): Çekirdek başlatıcı altyapısı
- **[Phosphor Icons](https://github.com/phosphor-icons/core)** (MIT): Simge setinin temeli, lisans metni [`design/icons/LICENSE.txt`](design/icons/LICENSE.txt) dosyasındadır
- Kullanılan tüm açık kaynak kütüphanelerin ve lisanslarının güncel listesi uygulama içinde **Ayarlar → Hakkında** ekranında bulunur.

<div align="center">

**LisyLauncher** · [AltayHR](https://github.com/ThT0AltayHR) tarafından geliştirilmektedir

</div>
