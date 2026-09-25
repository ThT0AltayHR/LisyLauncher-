# NDK_TOOLCHAIN_VERSION := 4.9
# mic_bridge.c, AAudio (<aaudio/AAudio.h>) API 26+ gerektirir; Gradle tarafında
# zaten minSdk=26 olduğu için bu yükseltme hiçbir cihaz desteğini kesmiyor.
APP_PLATFORM := android-26
APP_STL := c++_shared
# APP_ABI := armeabi-v7a arm64-v8a x86 x86_64
