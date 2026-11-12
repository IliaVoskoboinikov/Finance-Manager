# CrashlyticsLogger отправляет исключение без текста: тип исходного звена записан строкой в
# message копии (RedactedException). Crashlytics восстанавливает по mapping-файлу только фреймы
# и тип самого исключения, а не строки, — без имён типы были бы обфусцированы (a.b.c).
-keepnames class * extends java.lang.Throwable
