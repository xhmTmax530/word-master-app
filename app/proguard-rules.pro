# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in proguard-android-optimize.txt.

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# kotlinx.serialization
-keep,includedescriptorclasses class com.wordmaster.app.**$$serializer { *; }
-keepclassmembers class com.wordmaster.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.wordmaster.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}