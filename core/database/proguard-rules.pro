# Room database + converters
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}
-dontwarn java.lang.invoke.StringConcatFactory
