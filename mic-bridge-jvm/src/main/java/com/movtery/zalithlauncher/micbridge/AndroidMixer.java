package com.movtery.zalithlauncher.micbridge;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

/**
 * Android'in tek mikrofon girişini temsil eden {@link Mixer}. Sadece hedef
 * (kayıt/target) hat sağlar; kaynak (playback/source) hat sağlamaz — çünkü
 * oyunun kendi ses çıkışı zaten LWJGL/OpenAL üzerinden çalışıyor, burada
 * sadece mikrofon girişini köprülüyoruz.
 */
final class AndroidMixer implements Mixer {

    private static final Line.Info[] TARGET_LINE_INFOS = {AndroidTargetDataLine.LINE_INFO};
    private static final Line.Info[] SOURCE_LINE_INFOS = {};
    private static final Line.Info MIXER_LINE_INFO = new Line.Info(Mixer.class);

    private final Mixer.Info mixerInfo;
    private final List<Line> openLines = new ArrayList<>();
    private volatile boolean open;

    AndroidMixer() {
        this.mixerInfo = new AndroidMixerInfo(
                "Android Microphone (AAudio)",
                "LisyLauncher",
                "AAudio üzerinden gerçek mikrofon girişi köprüsü",
                "1.0"
        );
    }

    @Override
    public Mixer.Info getMixerInfo() {
        return mixerInfo;
    }

    @Override
    public Line.Info[] getSourceLineInfo() {
        return SOURCE_LINE_INFOS.clone();
    }

    @Override
    public Line.Info[] getTargetLineInfo() {
        return TARGET_LINE_INFOS.clone();
    }

    @Override
    public Line.Info[] getSourceLineInfo(Line.Info info) {
        return SOURCE_LINE_INFOS.clone();
    }

    @Override
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        if (matchesTargetDataLine(info)) {
            return new Line.Info[]{AndroidTargetDataLine.LINE_INFO};
        }
        return new Line.Info[0];
    }

    @Override
    public boolean isLineSupported(Line.Info info) {
        return matchesTargetDataLine(info);
    }

    @Override
    public Line getLine(Line.Info info) throws LineUnavailableException {
        if (!matchesTargetDataLine(info)) {
            throw new LineUnavailableException("Desteklenmeyen hat: " + info);
        }
        AndroidTargetDataLine line = new AndroidTargetDataLine();
        synchronized (openLines) {
            openLines.add(line);
        }
        return line;
    }

    /**
     * info.matches()'in JDK içindeki tam yönü (hangi tarafın "geniş", hangi
     * tarafın "istenen" sayıldığı) belgesizce güvenilecek kadar net değil;
     * bunun yerine burada kendi açık kontrolümüzü yapıyoruz — hem hangi
     * yönden çağrılırsa çağrılsın aynı sonucu verir, hem de anlaşılması kolay.
     */
    private static boolean matchesTargetDataLine(Line.Info info) {
        if (info == null) return false;
        if (!info.getLineClass().isAssignableFrom(TargetDataLine.class)) {
            return false;
        }
        if (info instanceof DataLine.Info) {
            AudioFormat[] requested = ((DataLine.Info) info).getFormats();
            if (requested.length == 0) {
                return true; //belirli bir format istenmiyor, genel destek yeterli
            }
            for (AudioFormat f : requested) {
                if (AndroidTargetDataLine.supportsFormat(f)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public int getMaxLines(Line.Info info) {
        if (matchesTargetDataLine(info)) {
            return 1; // Fiziksel olarak tek mikrofon girişi
        }
        return 0;
    }

    @Override
    public Line[] getSourceLines() {
        return new Line[0];
    }

    @Override
    public Line[] getTargetLines() {
        synchronized (openLines) {
            return openLines.toArray(new Line[0]);
        }
    }

    @Override
    public void synchronize(Line[] lines, boolean maintainSync) {
        // Birden fazla hattı senkronize başlatma/durdurma desteklenmiyor.
    }

    @Override
    public void unsynchronize(Line[] lines) {
        // no-op
    }

    @Override
    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        return false;
    }

    @Override
    public Line.Info getLineInfo() {
        return MIXER_LINE_INFO;
    }

    @Override
    public void open() {
        open = true;
    }

    @Override
    public void close() {
        synchronized (openLines) {
            for (Line line : openLines) {
                line.close();
            }
            openLines.clear();
        }
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public Control[] getControls() {
        return new Control[0];
    }

    @Override
    public boolean isControlSupported(Control.Type control) {
        return false;
    }

    @Override
    public Control getControl(Control.Type control) {
        throw new IllegalArgumentException("Desteklenmeyen kontrol: " + control);
    }

    @Override
    public void addLineListener(javax.sound.sampled.LineListener listener) {
        // Mixer seviyesinde olay bildirimi bu köprüde uygulanmadı.
    }

    @Override
    public void removeLineListener(javax.sound.sampled.LineListener listener) {
        // no-op
    }
}
