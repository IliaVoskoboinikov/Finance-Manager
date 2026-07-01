# ============================================================
#  Finance Manager — R8 / ProGuard keep-правила (release)
# ============================================================
# Retrofit, OkHttp, Room, Hilt, Coroutines и Firebase поставляют
# собственные consumer-правила (в своих артефактах), поэтому здесь
# описываем только то, что специфично для приложения: модели, которые
# создаются/заполняются рефлексией (Gson) и потому не видны R8 напрямую.

# --- Атрибуты, нужные Gson/Retrofit для дженериков и аннотаций ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# --- Gson DTO ---
# DTO десериализуются рефлексией; без keep R8 может удалить/переименовать
# поля, и парсинг JSON вернёт объекты с null/дефолтными значениями.
# Все DTO лежат в пакетах *.dto.* (core:data, core:auth).
-keep class soft.divan.financemanager.**.dto.** { *; }

# Поля с @SerializedName в любом классе (имя в JSON задаёт аннотация,
# поэтому обфускация имени поля допустима, а само поле удалять нельзя).
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
