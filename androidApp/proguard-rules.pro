# =======================================================================
# AR / Sceneform / Filament
# =======================================================================
# ARCore: нативные JNI-классы, рефлексия в LightEstimate/Frame/Session.
-keep class com.google.ar.core.** { *; }
-dontwarn com.google.ar.core.**

# Sceneform (Google archived) + форк gorisse + Filament.
# Внутри тяжёлая рефлексия по нодам, рендерам, материалам и JNI-биндингам.
-keep class com.google.ar.sceneform.** { *; }
-keep class com.gorisse.thomas.sceneform.** { *; }
-keep class com.google.android.filament.** { *; }
-keep class com.google.android.filament.utils.** { *; }
-keep class com.google.android.filament.gltfio.** { *; }
-dontwarn com.google.ar.sceneform.**
-dontwarn com.gorisse.thomas.sceneform.**
-dontwarn com.google.android.filament.**

# =======================================================================
# Kotlin / coroutines / serialization
# =======================================================================
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

# kotlinx.coroutines — внутренние Service-loader атомики.
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# kotlinx.serialization — @Serializable классы и их сериализаторы.
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class **$$serializer { *; }

# =======================================================================
# Ktor — движки подгружаются через ServiceLoader.
# =======================================================================
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.engine.okhttp.** { *; }
-keep class io.ktor.client.engine.android.** { *; }
-keep class io.ktor.utils.io.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# OkHttp / Okio.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# =======================================================================
# Koin — инжект через reflection по сигнатурам конструкторов.
# =======================================================================
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}
-keep class * extends org.koin.core.module.Module

# =======================================================================
# MVI state holders проекта — sealed-классы, читаются в логах и
# отправляются через Flow между shared и UI.
# =======================================================================
-keep class com.poroshin.rut.ar.common.mvi.UiState
-keep class com.poroshin.rut.ar.common.mvi.UiEvent
-keep class com.poroshin.rut.ar.common.mvi.UiAction
-keep class com.poroshin.rut.ar.common.mvi.SharedViewModel { *; }
-keep class * implements com.poroshin.rut.ar.common.mvi.UiState { *; }
-keep class * implements com.poroshin.rut.ar.common.mvi.UiEvent { *; }
-keep class * implements com.poroshin.rut.ar.common.mvi.UiAction { *; }

# =======================================================================
# Cicerone — навигация по reflection через Screen.
# =======================================================================
-keep class com.github.terrakok.cicerone.** { *; }

# =======================================================================
# Coil — image loader, ServiceLoader для декодеров.
# =======================================================================
-keep class coil3.** { *; }
-dontwarn coil3.**

# =======================================================================
# AndroidX Fragment / Compose — обычно покрывается consumer-rules,
# но добавим Compose Tooling preview, чтобы lint не падал.
# =======================================================================
-dontwarn androidx.compose.ui.tooling.**

# Compose Runtime: не убирать @Composable-функции через mangling.
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
