# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# kotlinx.serialization — keep serializer() for @Serializable classes.
# See https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nahunp.todoapp.**$$serializer { *; }
-keepclassmembers class com.nahunp.todoapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.nahunp.todoapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
