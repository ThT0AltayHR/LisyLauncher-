package com.movtery.zalithlauncher.micbridge;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

/**
 * Android AAudio girişini (mikrofon) bir {@link TargetDataLine} olarak sunar.
 * Sadece PCM_SIGNED, 16-bit, little-endian, 1-2 kanal formatlarını destekler
 * — bu, sesli sohbet modlarının (ör. Simple Voice Chat tarzı modlar) tipik
 * olarak istediği formattır. Desteklenmeyen bir format istenirse {@code open}
 * temiz bir şekilde {@link LineUnavailableException} fırlatır; sessizce
 * yanlış formatta veri üretmez.
 */
final class AndroidTargetDataLine implements TargetDataLine {

    private static final int DEFAULT_SAMPLE_RATE = 48000;

    // AudioSystem.getTargetDataLine(format) eşleşmesi somut formatlara bakar;
    // null/joker format semantiğine güvenmek yerine modların gerçekte istediği
    // yaygın oran/kanal kombinasyonlarını açıkça listeliyoruz.
    private static final AudioFormat[] ADVERTISED_FORMATS = buildAdvertisedFormats();

    private static AudioFormat[] buildAdvertisedFormats() {
        int[] rates = {8000, 11025, 16000, 22050, 32000, 44100, 48000};
        AudioFormat[] formats = new AudioFormat[rates.length * 2];
        int i = 0;
        for (int rate : rates) {
            formats[i++] = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, 1, 2, rate, false);
            formats[i++] = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, 2, 4, rate, false);
        }
        return formats;
    }

    static final Line.Info LINE_INFO = new DataLine.Info(
            TargetDataLine.class, ADVERTISED_FORMATS, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED);

    private final List<LineListener> listeners = new ArrayList<>();

    private long nativeHandle;
    private AudioFormat format;
    private int bufferSize;
    private volatile boolean open;
    private volatile boolean running;
    private long framePosition;

    @Override
    public void open(AudioFormat requestedFormat, int requestedBufferSize) throws LineUnavailableException {
        if (open) {
            throw new IllegalStateException("Line is already open");
        }
        validateFormat(requestedFormat);

        int sampleRate = (int) requestedFormat.getSampleRate();
        if (sampleRate <= 0) sampleRate = DEFAULT_SAMPLE_RATE;
        int channels = requestedFormat.getChannels();

        long handle = NativeMic.nativeOpenStream(sampleRate, channels);
        if (handle == 0) {
            throw new LineUnavailableException("AAudio giriş akışı açılamadı (mikrofon izni verilmemiş olabilir ya da cihaz desteklemiyor)");
        }

        int actualRate = NativeMic.nativeGetSampleRate(handle);
        int actualChannels = NativeMic.nativeGetChannelCount(handle);
        if (actualRate != sampleRate || actualChannels != channels) {
            // AAudio istenen formatı tam olarak veremedi; yanlış hızda/kanalda
            // ses üretmek yerine temiz bir şekilde vazgeçiyoruz.
            NativeMic.nativeClose(handle);
            throw new LineUnavailableException(
                    "İstenen format sağlanamadı (istenen: " + sampleRate + "Hz/" + channels +
                            "ch, cihazın verebildiği: " + actualRate + "Hz/" + actualChannels + "ch)");
        }

        this.nativeHandle = handle;
        this.format = requestedFormat;
        this.bufferSize = requestedBufferSize > 0
                ? requestedBufferSize
                : requestedFormat.getFrameSize() * (sampleRate / 10); // ~100ms varsayılan
        this.framePosition = 0;
        this.open = true;

        fireEvent(LineEvent.Type.OPEN);
    }

    @Override
    public void open(AudioFormat format) throws LineUnavailableException {
        open(format, -1);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (!open) {
            throw new IllegalStateException("Line is not open");
        }
        if (!running) {
            return 0;
        }

        int bytesRead = NativeMic.nativeRead(nativeHandle, b, off, len);
        if (bytesRead < 0) {
            return 0;
        }

        int frameSize = format.getFrameSize();
        if (frameSize > 0) {
            framePosition += bytesRead / frameSize;
        }
        return bytesRead;
    }

    @Override
    public void start() {
        if (!open || running) return;
        if (NativeMic.nativeStart(nativeHandle)) {
            running = true;
            fireEvent(LineEvent.Type.START);
        }
    }

    @Override
    public void stop() {
        if (!open || !running) return;
        NativeMic.nativeStop(nativeHandle);
        running = false;
        fireEvent(LineEvent.Type.STOP);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isActive() {
        return running;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public int available() {
        // AAudio bloklayan okumayla kullanılıyor; önceden kaç bayt hazır
        // olduğunu ucuz şekilde raporlayamıyoruz.
        return 0;
    }

    @Override
    public void drain() {
        // Bloklayan okuma modeli için anlamlı bir karşılığı yok; no-op.
    }

    @Override
    public void flush() {
        // AAudio'nun kendi iç tamponunu buradan temizlemiyoruz; no-op.
    }

    @Override
    public int getFramePosition() {
        return (int) framePosition;
    }

    @Override
    public long getLongFramePosition() {
        return framePosition;
    }

    @Override
    public long getMicrosecondPosition() {
        float sampleRate = format != null ? format.getSampleRate() : 0;
        if (sampleRate <= 0) return AudioSystem.NOT_SPECIFIED;
        return (long) (framePosition * 1_000_000.0 / sampleRate);
    }

    @Override
    public float getLevel() {
        return AudioSystem.NOT_SPECIFIED;
    }

    @Override
    public Line.Info getLineInfo() {
        return LINE_INFO;
    }

    @Override
    public void open() throws LineUnavailableException {
        throw new LineUnavailableException("TargetDataLine bir AudioFormat belirtilmeden açılamaz");
    }

    @Override
    public void close() {
        if (!open) return;
        stop();
        NativeMic.nativeClose(nativeHandle);
        nativeHandle = 0;
        open = false;
        fireEvent(LineEvent.Type.CLOSE);
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
    public void addLineListener(LineListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeLineListener(LineListener listener) {
        listeners.remove(listener);
    }

    private void fireEvent(LineEvent.Type type) {
        LineEvent event = new LineEvent(this, type, framePosition);
        for (LineListener listener : new ArrayList<>(listeners)) {
            listener.update(event);
        }
    }

    static boolean supportsFormat(AudioFormat requestedFormat) {
        if (requestedFormat == null) return false;
        boolean okEncoding = AudioFormat.Encoding.PCM_SIGNED.equals(requestedFormat.getEncoding());
        boolean okBits = requestedFormat.getSampleSizeInBits() == 16;
        boolean okEndian = !requestedFormat.isBigEndian();
        boolean okChannels = requestedFormat.getChannels() == 1 || requestedFormat.getChannels() == 2;
        return okEncoding && okBits && okEndian && okChannels;
    }

    private static void validateFormat(AudioFormat requestedFormat) throws LineUnavailableException {
        if (!supportsFormat(requestedFormat)) {
            throw new LineUnavailableException(
                    "Bu mikrofon köprüsü sadece 16-bit little-endian PCM_SIGNED, mono/stereo formatını destekliyor: " +
                            requestedFormat);
        }
    }
}
