# ===== MindSync Production ProGuard Rules =====

# --- Preserve stack traces for crash reporting ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- JNI / Native Methods ---
-keep class com.esh.offlineai.AIEngine {
    native <methods>;
    *;
}

# --- Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- AndroidX / Jetpack Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- PDFBox Android ---
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# --- Play Billing & ML Kit ---
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# --- Suppress common false-positive warnings ---
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn sun.misc.Unsafe
