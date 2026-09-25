package com.movtery.zalithlauncher.micbridge;

/**
 * mic_bridge.c (AAudio tabanlı) native köprüsüne ince bir Java sarmalayıcı.
 * <p>
 * Bu sınıf yüklenirken native kütüphane bulunamazsa (ör. beklenmedik bir
 * ortamda çalıştırılırsa, ya da .so bu ABI için paketlenmemişse) çökmek
 * yerine sessizce "kullanılamaz" durumuna geçer; gerçek kullanılabilirlik
 * kontrolü için {@link #isAvailable()} çağrılmalıdır ve native metodlar
 * kütüphane yüklenmeden asla çağrılmamalıdır.
 */
final class NativeMic {

    private static final boolean LIBRARY_LOADED;

    static {
        boolean loaded;
        try {
            System.loadLibrary("micbridge");
            loaded = true;
        } catch (Throwable t) {
            loaded = false;
        }
        LIBRARY_LOADED = loaded;
    }

    private NativeMic() {
    }

    /**
     * Native kütüphane yüklendi mi VE cihaz gerçekten bir AAudio giriş akışı
     * açabiliyor mu? Bu, gerçek bir akış açmadan yapılan hafif bir kontroldür.
     */
    static boolean isAvailable() {
        return LIBRARY_LOADED && nativeIsAvailable();
    }

    private static native boolean nativeIsAvailable();

    /**
     * @return başarılıysa 0'dan farklı bir opak akış tanıtıcısı (handle),
     * başarısızsa 0
     */
    static native long nativeOpenStream(int sampleRate, int channelCount);

    static native int nativeGetSampleRate(long handle);

    static native int nativeGetChannelCount(long handle);

    static native boolean nativeStart(long handle);

    /**
     * Bloklayan okuma. Veri gelene, durdurma istenene ya da bir hata oluşana
     * kadar bekler.
     *
     * @return okunan bayt sayısı, ya da durdurulduysa/hata oluştuysa negatif
     */
    static native int nativeRead(long handle, byte[] buffer, int offset, int length);

    static native void nativeStop(long handle);

    static native void nativeClose(long handle);
}
