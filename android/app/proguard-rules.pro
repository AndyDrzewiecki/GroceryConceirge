# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the AGP default file.  You can control the set of applied configuration
# files using the proguardFiles setting in build.gradle.

# Keep Retrofit interfaces.
-keep interface com.homeops.grocery.network.** { *; }

# Keep data model classes used by Gson.
-keep class com.homeops.grocery.network.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
