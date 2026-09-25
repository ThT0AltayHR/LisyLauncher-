package com.movtery.zalithlauncher.micbridge;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.MixerProvider;

/**
 * javax.sound.sampled.spi.MixerProvider'ın Android/AAudio uygulaması.
 * {@code AudioSystem}, META-INF/services/javax.sound.sampled.spi.MixerProvider
 * dosyası aracılığıyla bu sınıfı otomatik olarak bulur (bkz. bu modülün
 * kaynakları), böylece mikrofon erişimi isteyen modlar normal
 * {@code AudioSystem.getTargetDataLine(format)} çağrısıyla bu köprüyü
 * kullanabilir — mod tarafında hiçbir özel entegrasyon gerekmez.
 * <p>
 * Cihazda AAudio girişi kullanılamıyorsa (native kütüphane yüklenemedi ya da
 * cihaz desteklemiyor) hiçbir mixer bildirmiyoruz; bu durumda
 * {@code AudioSystem} normal şekilde "hat bulunamadı" hatası verir — modun
 * kendi var olan hata yönetimini bozmuyoruz.
 */
public final class AndroidMixerProvider extends MixerProvider {

    // Tek bir örnek yeterli: aynı fiziksel mikrofonu temsil ediyor,
    // her seferinde yeni Mixer.Info üretmek gereksiz karmaşıklık katar.
    private static final AndroidMixer MIXER = new AndroidMixer();

    @Override
    public Mixer.Info[] getMixerInfo() {
        if (!NativeMic.isAvailable()) {
            return new Mixer.Info[0];
        }
        return new Mixer.Info[]{MIXER.getMixerInfo()};
    }

    @Override
    public Mixer getMixer(Mixer.Info info) {
        if (info != null && !info.equals(MIXER.getMixerInfo())) {
            throw new IllegalArgumentException("Bilinmeyen mixer: " + info);
        }
        return MIXER;
    }
}
