# Testing & Coverage

Документ описывает подход к тестированию **Finance Manager**: какие уровни тестов есть,
какими инструментами они пишутся, как измеряется покрытие (Kover) и как всё это запускать.

## Цели

- Покрыть юнит-тестами всю тестируемую на JVM логику (домен, данные, синхронизация,
  ViewModel'и, мапперы) — целевой порог **95 %** по строкам.
- Проверять поведение, а не реализацию: happy-path, граничные случаи, сценарии ошибок.
- Держать быстрый фидбэк: основная масса тестов — обычные JVM-юниты без эмулятора.
- Изолировать «тяжёлые» тесты (Room, WorkManager, системные API) в Robolectric, но так,
  чтобы они по-прежнему запускались в `testDebugUnitTest` (без instrumentation/эмулятора).

## Пирамида тестов

```
        ┌───────────────────────────────────────────┐
        │  Gradle TestKit (build-logic)             │  реальные Gradle-сборки конвеншенов
        ├───────────────────────────────────────────┤
        │  Robolectric (Room DAO, WorkManager,      │  Android-фреймворк на JVM,
        │  ConnectivityManager, Locale, биометрия)  │  без эмулятора
        ├───────────────────────────────────────────┤
        │  JVM unit (домен, данные, sync, VM,       │  ~90 % всех тестов,
        │  мапперы, DI-фабрики)                      │  MockK + AssertJ + coroutines-test
        └───────────────────────────────────────────┘
```

## Инструменты

| Инструмент | Зачем |
|------------|-------|
| **JUnit 4** | Базовый раннер (`@Test`, `@Before`, `@RunWith`). |
| **MockK** | Моки зависимостей; `mockkStatic`/`mockkObject` для статических мостов (Firebase, `Log`, `EntryPointAccessors`, `WorkManager`, `VibrationEffect`, `BiometricManager`). |
| **AssertJ** | Флюентные ассерты (`assertThat(...).isEqualTo(...)`, `isEqualByComparingTo` для денег). |
| **kotlinx-coroutines-test** | `runTest`, `UnconfinedTestDispatcher`, `Dispatchers.setMain/resetMain` для тестов корутин и `StateFlow`. |
| **Robolectric** | Реализация Android-фреймворка на JVM: in-memory Room, `Shadow*` для `ConnectivityManager`, `NotificationManager` и т.п. |
| **androidx.work:work-testing** | `TestListenableWorkerBuilder` для `CoroutineWorker`. |
| **Gradle TestKit** | Функциональные тесты конвеншен-плагинов из `build-logic` через реальный `GradleRunner`. |
| **Kover** | Измерение и верификация покрытия (см. ниже). |

Стандартный набор (`JUnit + MockK + AssertJ + coroutines-test`) подключается автоматически
конвеншен-плагинами `soft.divan.core`, `soft.divan.jvm.library`, `soft.divan.feature.impl`
(бандл `unit-test` в `libs.versions.toml`, см. `TestDependencies.kt`). Модуль-специфичные
зависимости (Robolectric, `work-testing`, `androidx.test:core`) объявляются в `build.gradle.kts`
конкретного модуля.

## Что такое Kover

**Kover** (JetBrains Kotlin Coverage) — Gradle-плагин для измерения покрытия Kotlin-кода
тестами. Аналог JaCoCo, но «родной» для Kotlin: корректно понимает inline-функции,
корутины, `data class`, лямбды и не считает синтетический байт-код как непокрытый.

Подключён в корневом `build.gradle.kts` через `merge { allProjects() }` — это агрегирует
покрытие по **всем** модулям в один отчёт (`createVariant("full")`, объединяющий JVM- и
Android-`debug`-варианты). Ключевые задачи:

| Задача | Что делает |
|--------|-----------|
| `./gradlew koverHtmlReportFull` | HTML-отчёт: `build/reports/kover/htmlFull/index.html`. |
| `./gradlew koverXmlReportFull` | XML-отчёт (для CI/парсинга): `build/reports/kover/reportFull.xml`. |
| `./gradlew koverVerifyFull` | Проверяет порог `minBound(99)` по строкам; падает, если ниже. |

Фактическое покрытие тестируемого кода — **~99.6 %** по строкам.

### Что исключено из знаменателя

Метрика считает только **тестируемую** логику. В `kover { reports { filters { excludes { … } } } }`
исключены:

- **Сгенерированный код** — Hilt/Dagger (`*_Factory`, `*_HiltModules*`, `Hilt_*`, `Dagger*`),
  Room `*_Impl`, `BuildConfig`, `ComposableSingletons*`, `androidGeneratedClasses()`.
- **UI-слой** — всё с `@Composable`/`@Preview`, `*Activity`, `App`, navigation `*FeatureImpl`.
  Compose-тесты осознанно отложены (см. «Отложено»), поэтому экраны не входят в метрику.

Важно: `build-logic` (конвеншен-плагины) — это **included build**, он не попадает в
Kover-метрику основного проекта. Его тесты запускаются и считаются отдельно.

## Как запускать

```bash
# Все юнит-тесты (Debug), включая Robolectric
./gradlew testDebugUnitTest

# Чистые JVM-модули (core:domain, lint) — задача называется просто `test`
./gradlew :core:domain:test :lint:test

# Тесты конвеншен-плагинов (included build)
./gradlew :build-logic:convention:test

# Покрытие
./gradlew koverHtmlReportFull      # человекочитаемый отчёт
./gradlew koverVerifyFull          # гейт 98 %

# Полный локальный гейт «как в CI»
./gradlew testDebugUnitTest :lint:test :core:domain:test \
          ktlintCheck detekt koverVerifyFull
```

## Конвенции и типовые приёмы

- **ViewModel'и.** Диспетчер инжектится (`@IoDispatcher CoroutineDispatcher`), в тестах
  передаётся `UnconfinedTestDispatcher()`. Для `Dispatchers.Main` — `Dispatchers.setMain()`
  в `@Before` и `resetMain()` в `@After`. Для «горячих» `StateFlow` с `WhileSubscribed`
  подписка стартует через отдельную корутину-коллектор, которую в конце `job.cancel()`.
- **Деньги.** Сравнивать `BigDecimal` через `isEqualByComparingTo`, а не `isEqualTo`
  (scale отличается: `100` vs `100.00`).
- **Фоновая работа репозиториев.** Репозитории пускают синхронизацию через
  `AppCoroutineContext.launch {}`. В тестах вместо реального контекста —
  `RecordingAppCoroutineContext`: он не запускает блоки сразу, а записывает их, чтобы
  сначала проверить синхронный результат, а затем детерминированно выполнить фон (`runAll()`).
- **Статические мосты.** `Log`, `FirebaseCrashlytics`, `EntryPointAccessors`, `WorkManager`,
  `VibrationEffect`, `BiometricManager` — через `mockkStatic`/`mockkObject` с `unmockk*` в `@After`.
- **Room DAO.** In-memory база (`Room.inMemoryDatabaseBuilder`), базовый класс `RoomDaoTest`
  поднимает/закрывает её. Даты в `getByAccountAndPeriod` сравниваются строками с
  `date(..., 'localtime')`, поэтому в тестах отметки времени строятся от локального полудня —
  чтобы ожидания не зависели от таймзоны машины.
- **Gradle TestKit.** `GradleRunner` с `withPluginClasspath()` собирает временный проект во
  `TemporaryFolder`, применяет `soft.divan.check.conventions` и проверяет, что нарушения
  конвенций реально валят сборку (`buildAndFail()`), а корректная структура — проходит (`build()`).

## Покрытие по слоям

Юнит-тестами (JVM + Robolectric) покрыты:

- **Домен** (`core:domain`) — модели, `DomainResult`, use-case'ы, `UiDateFormatter`, мапперы enum.
- **Данные** (`core:data`) — мапперы, `safeApiCall`/`safeDbCall` (все ветки ошибок),
  4 репозитория, источники данных, 3 sync-менеджера (pull/push, last-write-wins),
  `RoomTransactionRunner`.
- **БД** (`core:database`) — 4 DAO и открытие базы из prepackaged-ассета (Robolectric).
- **Сеть** (`core:network`) — интерцепторы, `BigDecimalTypeAdapter`, DI OkHttp/Retrofit,
  `ConnectivityManagerNetworkMonitor` (Robolectric).
- **Синхронизация** (`sync`) — репозиторий, use-case'ы, `SyncCoordinator`, планировщик
  WorkManager, `SyncWorker`/`DelegatingWorker`/уведомления (Robolectric).
- **Фичи** — все ViewModel'и (состояния, события, ошибки), use-case'ы, мапперы, DataStore-источники.
- **build-logic** — `CheckConventionsPlugin` (проверки плагинов и архитектурных зависимостей),
  `generateNamespace`, константы — ProjectBuilder + Gradle TestKit.

## Отложено (stage 2 / отдельные инфраструктуры)

- **Compose UI** — экраны и компоненты `core:uikit`. Требуют Robolectric + `compose-ui-test`
  и/или Paparazzi (скриншот-тесты). Исключены из Kover-метрики, тесты пока не пишутся.
- **Настоящие миграции Room** — до релиза нужны `Migration` + `MigrationTestHelper`
  (instrumented), сейчас БД на `fallbackToDestructiveMigration` (см. `bd.md`).
- **CI-гейт по покрытию** — `koverVerifyFull` считается локально; отдельный job в CI пока не заведён.
