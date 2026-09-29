# ProGuard rules for Pace
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# Keep data classes and serialization for Routine and settings
-keepclassmembers class * {
    companion <fields>;
}
-keepclassmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keep class ch.simibu.pace.model.** { *; }
