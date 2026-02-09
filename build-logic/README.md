# Build Logic (Convention Plugins)

Этот модуль **build-logic** содержит набор convention-плагинов Gradle, которые инкапсулируют общую build‑конфигурацию проекта и позволяют:

- централизовать настройки Android / Kotlin / JVM
- уменьшить дублирование `build.gradle.kts`
- стандартизировать архитектуру модулей
- ускорить и упростить масштабирование проекта

Модуль реализован с использованием **Gradle Version Catalog**, **Kotlin DSL** и best‑practice подхода *Convention over Configuration*.

---

## Структура модуля

```
build-logic/
├── convention/
│   ├── AndroidBaseConventionPlugin
│   ├── AndroidAppConventionPlugin
│   ├── AndroidAppFirebaseConventionPlugin
│   ├── CoreConventionPlugin
│   ├── FeatureApiConventionPlugin
│   ├── FeatureImplConventionPlugin
│   ├── HiltConventionPlugin
│   ├── JvmLibraryConventionPlugin
│   ├── BuildTimeTrackerConventionPlugin
│   └── utils (libs, Conf, namespace, android config)
├── settings.gradle.kts
└── build.gradle.kts
```

---

## Общие утилиты

### `Const`
Единая точка для базовых констант проекта:

- `NAMESPACE`
- `COMPILE_SDK`
- `MIN_SDK`
- `JAVA_VERSION`
- `VERSION_CODE / VERSION_NAME`

Используется всеми Android‑плагинами.

---

### `Conf`
Абстракция над Gradle конфигурациями:

- `implementation`
- `debugImplementation`
- `api`
- `ksp`

Позволяет избежать строковых литералов в плагинах.

---

### Version Catalog helpers

Упрощённый доступ к `libs.versions.toml`:

- `Project.libs`
- `Project.lib(alias)`
- `Project.version(alias)`
- `Project.applyPlugin(alias)`

---

### Namespace generation

```kotlin
fun generateNamespace(project: Project): String
```

Namespace формируется автоматически на основе `project.path`:

```
:feature:auth → soft.divan.financemanager.feature.auth
```

Это избавляет от ручного задания `namespace` в каждом модуле.

---

## Android Base Configuration

### `configureBaseAndroid()`

Общая Android‑конфигурация для **application** и **library** модулей:

- `compileSdk`
- `minSdk`
- `namespace`
- `release` buildType с ProGuard
- `Java/Kotlin compatibility`

Используется всеми Android convention‑плагинами.

---

## Compose dependencies

### `addDefaultComposeDependencies()`

Подключает базовый набор Compose‑зависимостей:

- Compose BOM
- `ui-tooling-preview`
- `material3`
- `hilt-navigation-compose`

Используется в `app` и `feature:impl` модулях.

---

## Convention плагины

### `soft-divan-android-base`
**Базовый Android плагин**

Применяет:

- `kotlin-android`
- `ktlint`
- общие Kotlin compiler options
- кастомные lint checks (`:lint`)

Используется всеми Android модулями.

---

### `soft-divan-android-app`
**Application‑модуль**

Применяет:

- android application
- compose
- hilt
- firebase
- graph plugin
- build time tracker

Настраивает:

- versionCode / versionName
- targetSdk
- Compose
- стандартные Compose зависимости

---

### `soft-divan-firebase`
**Firebase для application**

Подключает:

- Firebase BOM
- Crashlytics

Конфигурация по buildType:

- `debug` → crashlytics disabled
- `release` → crashlytics enabled

---

### `soft-divan-core`
**Core / common Android library**

- `android-library`
- `soft-divan-android-base`

Без Compose и DI.

---

### `soft-divan-feature-api`
**API слой фичи**

Используется для:

- contracts
- interfaces
- public models

Без Compose и реализаций.

---

### `soft-divan-feature-impl`
**Реализация фичи**

Применяет:

- android library
- compose
- hilt

Подключает:

- Compose dependencies
- `ui-tooling` для debug

---

### `soft-divan-hilt`
**Hilt DI**

Применяет:

- `hilt`
- `ksp`

Подключает:

- `hilt-android`
- `hilt-compiler`

---

### `soft-divan-jvm-library`
**Чистый JVM / domain модуль**

Применяет:

- `java-library`
- `kotlin-jvm`
- `ktlint`

Подключает:

- `kotlinx-coroutines-core`
- `javax.inject`

---

### `soft-divan-build-time-tracker`
**Анализ времени сборки**

Настройки:

- CSV отчёты
- сортировка по времени
- min task duration = 1s
- отчёты в `build/reports/buildTimeTracker`

---

## Подключение в проект

`settings.gradle.kts` основного проекта:

```kotlin
includeBuild("build-logic")
```

Использование в модулях:

```kotlin
plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}
```