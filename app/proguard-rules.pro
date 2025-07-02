# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#############################################
# Eternal Glory Game Specific Rules
#############################################

# Keep your main application class
-keep public class io.github.nihalhorseless.eternalglory.GameApplication {
    public <init>();
}

# Keep all your game model classes (they might be serialized/deserialized)
-keep class io.github.nihalhorseless.eternalglory.data.model.** { *; }
-keep class io.github.nihalhorseless.eternalglory.data.enum.** { *; }

# Keep database entities and DAOs for Room
-keep class io.github.nihalhorseless.eternalglory.data.db.** { *; }

# Keep ability classes that might be instantiated dynamically
-keep class io.github.nihalhorseless.eternalglory.data.model.abilities.** { *; }

# Keep effect classes for tactic cards
-keep class io.github.nihalhorseless.eternalglory.data.model.effect.** { *; }

# Keep game mechanics that might be referenced by string
-keep class io.github.nihalhorseless.eternalglory.game.** { *; }

# Keep ViewModels and their factory
-keep class io.github.nihalhorseless.eternalglory.ui.viewmodel.** { *; }

# Keep custom views and composables that might be referenced in navigation
-keep class io.github.nihalhorseless.eternalglory.ui.screens.** { *; }
-keep class io.github.nihalhorseless.eternalglory.ui.components.** { *; }

# Keep audio managers
-keep class io.github.nihalhorseless.eternalglory.audio.** { *; }

# Keep utility classes
-keep class io.github.nihalhorseless.eternalglory.util.** { *; }

# Keep navigation-related classes
-keep class io.github.nihalhorseless.eternalglory.ui.navigations.** { *; }

#############################################
# Android and AndroidX Rules
#############################################

# Keep Android lifecycle classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
}

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class * extends androidx.compose.runtime.Composer

# Navigation Component
-keep class androidx.navigation.** { *; }

#############################################
# Third-party Library Rules
#############################################

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Gson (for JSON parsing)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep your custom Gson adapters
-keep class io.github.nihalhorseless.eternalglory.data.storage.CardTypeAdapter { *; }
-keep class io.github.nihalhorseless.eternalglory.data.storage.AbilityTypeAdapter { *; }
-keep class io.github.nihalhorseless.eternalglory.data.storage.TacticCardDeserializer { *; }

# Coil (image loading)
-keep class coil3.** { *; }
-keep class coil3.decode.** { *; }
-keep class coil.** { *; }
-keepnames class coil3.gif.GifDecoder

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

#############################################
# Kotlin Specific Rules
#############################################

# Keep Kotlin metadata
-keepattributes *Annotation*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,AnnotationDefault
-keep class kotlin.Metadata { *; }


# Keep companion objects
-keep class io.github.nihalhorseless.eternalglory.** {
    public static ** Companion;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#############################################
# R8/ProGuard Configuration
#############################################

# Keep generic type information for Gson
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# Optimization settings
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#############################################
# Additional Safety Rules
#############################################

# Keep all resources
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#############################################
# Warnings to Ignore (if needed)
#############################################

# Add specific -dontwarn rules here if you encounter warnings during build
# Example:
# -dontwarn org.slf4j.**