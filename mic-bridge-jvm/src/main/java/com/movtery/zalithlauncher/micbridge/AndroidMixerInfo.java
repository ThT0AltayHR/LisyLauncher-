package com.movtery.zalithlauncher.micbridge;

import javax.sound.sampled.Mixer;

/**
 * {@link Mixer.Info}'nun kurucusu protected olduğu için, javax.sound.sampled
 * paketinin dışından bir örnek oluşturmanın standart yolu bu şekilde küçük
 * bir alt sınıf tanımlamaktır.
 */
final class AndroidMixerInfo extends Mixer.Info {

    AndroidMixerInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }
}
