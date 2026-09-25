/*
 * mic_bridge.c
 *
 * Gerçek mikrofon köprüsü: Android'in AAudio C API'sini (API 26+, minSdk ile
 * birebir uyumlu) kullanarak ham PCM verisini yakalar ve JNI üzerinden
 * gömülü JVM'de çalışan javax.sound.sampled SPI katmanına (bkz. mic-bridge-jvm
 * modülü, com.movtery.zalithlauncher.micbridge paketi) aktarır.
 *
 * Not (dürüst sınır): Bu dosya bu ortamda derlenip test edilemedi (bu kum
 * havuzunda NDK/Android SDK yok). Mantık AAudio'nun resmi blocking-read
 * API'sine göre yazıldı; gerçek cihazda test edilmesi gerekiyor.
 */

#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <aaudio/AAudio.h>
#include <dlfcn.h>

#define TAG "MicBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

typedef struct {
    AAudioStream *stream;
    int32_t sampleRate;
    int32_t channelCount;
    int32_t bytesPerFrame;
    volatile int32_t stopRequested;
} mic_stream_t;

// Tek bir okuma denemesindeki blocking timeout (nanosaniye).
// Küçük tutulur ki nativeStop çağrısı çok gecikmeden okuma döngüsünden çıkabilsin.
#define READ_TIMEOUT_NANOS (200LL * 1000000LL) // 200ms

/*
 * AAudioStreamBuilder_setInputPreset is annotated as API 28 in the NDK
 * headers. Resolve it dynamically on API 26/27 so the microphone bridge
 * remains compatible with the app's minSdk instead of failing native
 * compilation or loading an unavailable symbol on older devices.
 */
static void setVoiceCommunicationPreset(AAudioStreamBuilder *builder) {
#if __ANDROID_API__ >= 28
    AAudioStreamBuilder_setInputPreset(builder, AAUDIO_INPUT_PRESET_VOICE_COMMUNICATION);
#else
    typedef void (*SetInputPresetFn)(AAudioStreamBuilder *, aaudio_input_preset_t);
    SetInputPresetFn setInputPreset =
            (SetInputPresetFn) dlsym(RTLD_DEFAULT, "AAudioStreamBuilder_setInputPreset");
    if (setInputPreset != NULL) {
        setInputPreset(builder, AAUDIO_INPUT_PRESET_VOICE_COMMUNICATION);
    }
#endif
}

JNIEXPORT jlong JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeOpenStream(
        JNIEnv *env, jclass clazz, jint sampleRate, jint channelCount) {
    (void) env;
    (void) clazz;

    AAudioStreamBuilder *builder = NULL;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK || builder == NULL) {
        LOGE("createStreamBuilder failed: %s", AAudio_convertResultToText(result));
        return 0;
    }

    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_INPUT);
    AAudioStreamBuilder_setSampleRate(builder, sampleRate);
    AAudioStreamBuilder_setChannelCount(builder, channelCount);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    // Modlar (ör. sesli sohbet modları) genelde konuşma amaçlı yakalama ister.
    setVoiceCommunicationPreset(builder);

    AAudioStream *stream = NULL;
    result = AAudioStreamBuilder_openStream(builder, &stream);
    AAudioStreamBuilder_delete(builder);

    if (result != AAUDIO_OK || stream == NULL) {
        LOGE("openStream failed: %s", AAudio_convertResultToText(result));
        return 0;
    }

    mic_stream_t *handle = (mic_stream_t *) malloc(sizeof(mic_stream_t));
    if (handle == NULL) {
        AAudioStream_close(stream);
        return 0;
    }

    handle->stream = stream;
    handle->sampleRate = AAudioStream_getSampleRate(stream);
    handle->channelCount = AAudioStream_getChannelCount(stream);
    handle->bytesPerFrame = handle->channelCount * (int32_t) sizeof(int16_t);
    handle->stopRequested = 0;

    LOGI("mic stream opened: rate=%d channels=%d", handle->sampleRate, handle->channelCount);

    return (jlong) (intptr_t) handle;
}

JNIEXPORT jint JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeGetSampleRate(
        JNIEnv *env, jclass clazz, jlong handlePtr) {
    (void) env;
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    return handle != NULL ? handle->sampleRate : 0;
}

JNIEXPORT jint JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeGetChannelCount(
        JNIEnv *env, jclass clazz, jlong handlePtr) {
    (void) env;
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    return handle != NULL ? handle->channelCount : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeStart(
        JNIEnv *env, jclass clazz, jlong handlePtr) {
    (void) env;
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    if (handle == NULL || handle->stream == NULL) return JNI_FALSE;

    handle->stopRequested = 0;
    aaudio_result_t result = AAudioStream_requestStart(handle->stream);
    if (result != AAUDIO_OK) {
        LOGE("requestStart failed: %s", AAudio_convertResultToText(result));
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT jint JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeRead(
        JNIEnv *env, jclass clazz, jlong handlePtr, jbyteArray buffer, jint offset, jint length) {
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    if (handle == NULL || handle->stream == NULL || handle->bytesPerFrame <= 0) {
        return -1;
    }

    int32_t numFrames = length / handle->bytesPerFrame;
    if (numFrames <= 0) {
        return 0;
    }

    size_t scratchSize = (size_t) numFrames * (size_t) handle->bytesPerFrame;
    int16_t *scratch = (int16_t *) malloc(scratchSize);
    if (scratch == NULL) {
        return -1;
    }

    // stopRequested işaretlenene kadar kısa zaman aşımlarıyla tekrar dener;
    // böylece nativeStop çağrısı uzun süre bloklanmadan döngüden çıkabilir.
    aaudio_result_t framesRead = 0;
    while (!handle->stopRequested) {
        framesRead = AAudioStream_read(handle->stream, scratch, numFrames, READ_TIMEOUT_NANOS);
        if (framesRead != 0) break; // veri geldi ya da gerçek bir hata oluştu
    }

    if (handle->stopRequested) {
        free(scratch);
        return -1;
    }

    if (framesRead < 0) {
        LOGE("AAudioStream_read error: %s", AAudio_convertResultToText((aaudio_result_t) framesRead));
        free(scratch);
        return -1;
    }

    jint bytesRead = (jint) framesRead * handle->bytesPerFrame;
    (*env)->SetByteArrayRegion(env, buffer, offset, bytesRead, (const jbyte *) scratch);
    free(scratch);
    return bytesRead;
}

JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeStop(
        JNIEnv *env, jclass clazz, jlong handlePtr) {
    (void) env;
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    if (handle == NULL || handle->stream == NULL) return;

    handle->stopRequested = 1;
    AAudioStream_requestStop(handle->stream);
}

JNIEXPORT void JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeClose(
        JNIEnv *env, jclass clazz, jlong handlePtr) {
    (void) env;
    (void) clazz;
    mic_stream_t *handle = (mic_stream_t *) (intptr_t) handlePtr;
    if (handle == NULL) return;

    if (handle->stream != NULL) {
        handle->stopRequested = 1;
        AAudioStream_requestStop(handle->stream);
        AAudioStream_close(handle->stream);
    }
    free(handle);
}

JNIEXPORT jboolean JNICALL
Java_com_movtery_zalithlauncher_micbridge_NativeMic_nativeIsAvailable(
        JNIEnv *env, jclass clazz) {
    (void) env;
    (void) clazz;
    AAudioStreamBuilder *builder = NULL;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result == AAUDIO_OK && builder != NULL) {
        AAudioStreamBuilder_delete(builder);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
