# 🛠 Полезные команды

Шпаргалка по проекту **Finance Manager**: сборка, проверки, отчёты, графы и отладка на
устройстве. Все команды запускаются **из корня репозитория** и проверены на этом проекте
(Gradle 9.4.1, AGP 9.2.1, Kotlin 2.4.0).

> Подробности по каждой теме — в [`docs/`](docs/README.md); ссылки указаны в конце разделов.
> Технический бэклог — в [TODO.md](TODO.md).

## Содержание

- [Окружение и первый запуск](#окружение-и-первый-запуск)
- [Сборка и установка](#сборка-и-установка)
- [Тесты и покрытие](#тесты-и-покрытие)
- [Стиль и статический анализ](#стиль-и-статический-анализ)
- [Архитектура и граф модулей](#архитектура-и-граф-модулей)
- [Карта навигации (nav-graph)](#карта-навигации-nav-graph)
- [Размер приложения и время сборки](#размер-приложения-и-время-сборки)
- [Полный прогон «как в CI»](#полный-прогон-как-в-ci)
- [CI/CD: чем запускается какой пайплайн](#cicd-чем-запускается-какой-пайплайн)
- [Gradle: флаги и диагностика](#gradle-флаги-и-диагностика)
- [Если что-то сломалось](#если-что-то-сломалось)
- [Эмулятор](#эмулятор)
- [ADB: отладка на устройстве](#adb-отладка-на-устройстве)
- [База данных](#база-данных)
- [Где лежат отчёты](#где-лежат-отчёты)

---

## Окружение и первый запуск

### `local.properties`

Файл не коммитится. Минимальный набор ключей:

| Ключ | Зачем |
|------|-------|
| `sdk.dir` | путь к Android SDK (проставляет Android Studio) |
| `API_TOKEN` | токен бэкенда → `BuildConfig.API_TOKEN` в `:core:network` |
| `YANDEX_CLIENT_ID` | client_id Яндекс OAuth → `manifestPlaceholders` в `:app` |

Те же значения читаются из переменных окружения — так работает CI
(`getSecret()` в `app/build.gradle.kts` и `core/network/build.gradle.kts`:
сначала `System.getenv`, потом `local.properties`).

### `app/google-services.json`

**Обязателен** — без него падает плагин `com.google.gms.google-services`. В git не хранится
(`.gitignore`), в CI создаётся из секрета `GOOGLE_SERVICES_JSON` (base64) экшеном
[`create-google-services`](.github/actions/create-google-services/action.yml):

```bash
echo "$GOOGLE_SERVICES_JSON" | base64 --decode > app/google-services.json
```

### JDK

| Где | JDK |
|-----|-----|
| Локально | **21** — Gradle-демон уже на нём (`./gradlew --version` → `Launcher JVM: 21`) |
| CI | **17** ([`init-gradle`](.github/actions/init-gradle/action.yaml)), кроме джобы `nav-graph` |
| Рендер превью `navgraph` (Layoutlib 16.2.1) | **только 21** — на 17 падает `UnsupportedClassVersionError` и все превью выходят «no preview» |

```bash
./gradlew --version        # какая JVM реально используется Gradle
./gradlew javaToolchains   # какие JDK видит Gradle
./gradlew --stop           # прибить демонов (после смены JDK — обязательно)
```

### Первая сборка

```bash
./gradlew :app:installDebug
```

---

## Сборка и установка

| Команда | Что делает |
|---------|-----------|
| `./gradlew :app:assembleDebug` | debug-APK → `app/build/outputs/apk/debug/app-debug.apk` |
| `./gradlew :app:installDebug` | собрать и поставить на подключённое устройство/эмулятор |
| `./gradlew :app:uninstallDebug` | снести debug-сборку |
| `./gradlew :app:assembleRelease` | release-APK: R8 + shrink ресурсов + подпись ⚠️ см. ниже |
| `./gradlew :app:bundleRelease` | AAB для Google Play |
| `./gradlew :app:signingReport` | SHA-1 / SHA-256 отпечатки (нужны для Firebase и Яндекс OAuth) |
| `./gradlew -q :app:printVersionName` | печатает `Const.VERSION_NAME`; ⚠️ не знает про `-PversionName` (см. [TODO → CI/CD](TODO.md)) |
| `./gradlew clean` | снести `build/` |

### applicationId по типам сборки

| Build type | applicationId |
|------------|---------------|
| `debug` | `soft.divan.financemanager.debug` (`applicationIdSuffix = ".debug"`) |
| `release` | `soft.divan.financemanager` |

Это важно для всех `adb`-команд ниже — на устройстве debug и release живут рядом.

### Релизная сборка локально

Версия задаётся проектными свойствами (иначе берётся `Const.VERSION_CODE` / `Const.VERSION_NAME`),
пароли подписи — переменными окружения:

```bash
KEYSTORE_PASSWORD=… KEY_ALIAS=… KEY_PASSWORD=… \
./gradlew :app:assembleRelease -PversionName=0.1.0 -PversionCode=1000
```

Keystore берётся из **`app/release.jks`** (`./gradlew :app:signingReport` покажет, куда именно
смотрит конфиг). Файл `release.jks` в корне репозитория сборкой **не используется** — его надо
положить в `app/`; в CI туда его кладёт CD-workflow из секрета `JKS_KS`.

> ⚠️ **Сейчас `assembleRelease` падает** на `:app:minifyReleaseWithR8`:
> `Missing class kotlinx.parcelize.Parcelize (referenced from: com.yandex.authsdk.YandexAuthLoginOptions)`.
> Готовое правило R8 генерирует сам — `app/build/outputs/mapping/release/missing_rules.txt`
> (`-dontwarn kotlinx.parcelize.Parcelize`), его нужно перенести в `app/proguard-rules.pro`.
> Через ту же задачу идут `:app:bundleRelease` и `:app:analyzeReleaseBundle` — они тоже упадут.
> Причина, по которой это всплыло
> только сейчас: CI не собирает release (см. [TODO → CI/CD](TODO.md)).

В CD версия вычисляется из имени ветки `releases/**v.X.Y.Z`
(`VERSION_CODE = MAJOR*1000000 + MINOR*1000 + PATCH`) — см. [docs/ci-cd.md](docs/ci-cd.md).

---

## Тесты и покрытие

| Команда | Что делает |
|---------|-----------|
| `./gradlew testDebugUnitTest` | все юнит-тесты Android-модулей (JVM + Robolectric) |
| `./gradlew :core:domain:test :lint:test` | чистые JVM-модули — у них задача называется просто `test` |
| `./gradlew :build-logic:convention:test` | тесты конвеншен-плагинов (included build, в Kover не попадает) |
| `./gradlew :feature:<name>:impl:check` | полный набор проверок одного модуля (тесты + ktlint + lint) |

Точечный запуск:

```bash
# Android-модуль
./gradlew :feature:transaction:impl:testDebugUnitTest --tests "*TransactionViewModel*"

# JVM-модуль (:core:domain, :lint)
./gradlew :core:domain:test --tests "*GetTransactionsByPeriodUseCase*"
```

> Инструментальных тестов в проекте **пока нет** — папки `src/androidTest` пустые, поэтому
> `connectedDebugAndroidTest` ничего не выполняет. Они понадобятся для миграционных тестов
> Room (см. [TODO → Релиз](TODO.md)).

### Покрытие (Kover)

⚠️ **Важно: ведущее двоеточие.** `:koverVerifyFull` — задача **корневого** проекта, она считает
агрегированный отчёт и проверяет порог. Без двоеточия Gradle запустит одноимённую задачу
**в каждом модуле**: посчитает по-модульные проценты, а гейт не сработает вовсе (правила
`verify` заданы только в корневом `build.gradle.kts`). CI использует форму с двоеточием.

| Команда | Что делает |
|---------|-----------|
| `./gradlew :koverLogFull` | одна строка `application line coverage: NN%` по всему проекту |
| `./gradlew :koverHtmlReportFull` | HTML-отчёт → `build/reports/kover/htmlFull/index.html` |
| `./gradlew :koverXmlReportFull` | XML-отчёт → `build/reports/kover/reportFull.xml` |
| `./gradlew :koverVerifyFull` | гейт: падает, если ниже порога `minBound` |

Порог и список исключений (сгенерированный код, `@Composable`, `*Activity`, `*FeatureImpl`)
живут в корневом [`build.gradle.kts`](build.gradle.kts) — единственный источник истины.

📖 [docs/testing.md](docs/testing.md)

---

## Стиль и статический анализ

| Команда | Что делает |
|---------|-----------|
| `./gradlew --continue ktlintCheck` | проверка форматирования всех модулей |
| `./gradlew --continue ktlintFormat` | автоформатирование |
| `./gradlew ktlintGenerateBaseline` | baseline ktlint |
| `./gradlew detekt` | статический анализ; одна агрегированная задача на весь проект |
| `./gradlew detektBaseline` | снять baseline detekt (чтобы он применялся, путь надо прописать в `detekt { baseline = … }`) |
| `./gradlew lint` | Android Lint + кастомные правила модуля `:lint` (например `OldDate`) |
| `./gradlew :app:lintDebug` | lint по одному варианту одного модуля (быстрее) |
| `./gradlew lintFix` | применить безопасные автофиксы |
| `./gradlew updateLintBaseline` | обновить baseline Android Lint |

Конфиги: [`config/detekt/detekt.yml`](config/detekt/detekt.yml), ktlint настраивается в
блоке `subprojects { … }` корневого `build.gradle.kts`.

📖 [docs/agents/coding-style.md](docs/agents/coding-style.md)

---

## Архитектура и граф модулей

| Команда | Что делает |
|---------|-----------|
| *(любой запуск Gradle)* | `CheckConventionsPlugin` — **реальная** проверка архитектуры: core ↛ feature, api ↛ impl, impl ↛ impl, применение конвеншен-плагинов |
| `./gradlew :app:assertModuleGraph` | ⚠️ сейчас ничего не проверяет (`UP-TO-DATE`) — правила `moduleGraphAssert { … }` не настроены, см. [TODO → CI/CD](TODO.md) |
| `./gradlew :app:generateModulesGraphStatistics` | статистика графа: число модулей, рёбер, высота, самый длинный путь |
| `./gradlew :app:generateModulesGraphvizText -Pmodules.graph.output.gv=docs/graphs/modules_prodget/all_modules` | DOT-описание графа модулей |
| `./gradlew projects` | дерево всех модулей проекта |

Отрисовать DOT в PNG (нужен graphviz: `brew install graphviz`):

```bash
dot -Tpng docs/graphs/modules_prodget/all_modules -o docs/graphs/modules_prodget/all_modules.png
```

Зависимости модуля:

```bash
./gradlew :feature:transaction:impl:dependencies --configuration debugRuntimeClasspath
./gradlew :app:dependencyInsight --configuration debugRuntimeClasspath --dependency retrofit
```

Отчёт AGP о «лишних/недостающих» зависимостях (эвристика — к советам относиться критично,
он, например, предлагает выкинуть BOM'ы):

```bash
./gradlew :app:analyzeDebugDependencies
cat app/build/intermediates/analyze_dependencies_report/debug/analyzeDebugDependencies/analyzeDependencies/dependenciesReport.json
```

📖 [docs/modularization.md](docs/modularization.md), [docs/modules.md](docs/modules.md)

---

## Карта навигации (nav-graph)

Граф экранов собирается из аннотаций `@NavDestination` / `@NavEdge` / `@NavPreview`
плагином `compose-nav-graph`.

| Команда | Что делает |
|---------|-----------|
| `./gradlew navCheck` | ⚠️ обязательна после правки навигации: падает, если граф разошёлся с `*/nav/*.nav` |
| `./gradlew navDump` | перезаписать `.nav`-бейзлайны (коммитим вместе с изменением графа) |
| `./gradlew :app:aggregateNavGraph` | склеить графы всех модулей → `app/build/navgraph-aggregated/nav-graph.json` |
| `./gradlew :app:exportNavGraphHtml` | интерактивный HTML-граф → `app/build/navgraph/` |
| `./gradlew :app:exportNavGraphImage` | PNG-граф → `app/build/navgraph/` |
| `./gradlew :app:exportPreviewGalleryHtml` | галерея всех `@Preview` (HTML) → `app/build/navgallery/` |
| `./gradlew :app:exportPreviewGalleryImage` | галерея всех `@Preview` (PNG) → `app/build/navgallery/` |
| **`./gradlew :app:exportNavGraphToDocs`** | **экспорт графа и галереи сразу в `docs/graphs/nav_graph/`** — эту папку коммитим |

Рендер миниатюр требует **JDK 21**. Если Gradle-JVM у вас 17 — задайте её точечно:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:exportNavGraphToDocs
```

Типовой цикл после добавления экрана: разметить аннотациями → `./gradlew navDump` →
закоммитить `.nav` → при желании `:app:exportNavGraphToDocs`.

📖 [docs/nav-graph.md](docs/nav-graph.md), [docs/navigation3.md](docs/navigation3.md)

---

## Размер приложения и время сборки

| Команда | Что делает |
|---------|-----------|
| `./gradlew :app:analyzeDebugBundle --no-configuration-cache` | отчёт Ruler → `app/build/reports/ruler/debug/report.html` (+ `report.json`) |
| `./gradlew :app:assembleDebug` | побочно пишет тайминги задач → `app/build/reports/buildTimeTracker/build.csv` |
| `./gradlew :app:assembleDebug --profile` | HTML-профиль сборки → `build/reports/profile/` |

> ⚠️ Ruler **несовместим с configuration cache** (он включён в `gradle.properties`), поэтому без
> `--no-configuration-cache` задача падает с
> `error writing value of type 'kotlin.SynchronizedLazyImpl'`. В CI это работает потому, что там
> Gradle всегда запускается с `--no-configuration-cache`.

---

## Полный прогон «как в CI»

CI гоняет джобы: `assembleDebug`, `test`, `koverVerifyFull`, `lint`, `detekt`, `ktlintCheck`,
`:app:assertModuleGraph`, размер приложения, время сборки и `nav-graph`.

Локальный эквивалент перед пушем (проверено — проходит целиком):

```bash
./gradlew assembleDebug testDebugUnitTest :core:domain:test :lint:test \
          :koverVerifyFull lint detekt ktlintCheck navCheck :app:assertModuleGraph
```

Быстрая проверка одного затронутого модуля:

```bash
./gradlew :feature:<name>:impl:check ktlintCheck detekt
```

Флаги, которыми CI запускает Gradle (полезно воспроизвести при странных падениях):

```bash
./gradlew <task> --parallel --stacktrace --no-configuration-cache --no-daemon
```

📖 [docs/ci-cd.md](docs/ci-cd.md)

---

## CI/CD: чем запускается какой пайплайн

Пайплайны триггерятся **именем ветки** — отдельной команды «запустить CD» нет, достаточно пуша.

| Workflow | Триггер | Что делает |
|----------|---------|-----------|
| [`ci.yml`](.github/workflows/ci.yml) | любой `push` (кроме `**.md`) | 8 джоб проверок + отчёты |
| [`nav-graph.yml`](.github/workflows/nav-graph.yml) | любой `push` (кроме `**.md`) | рендер карты навигации и галереи превью |
| [`cd_tests.yml`](.github/workflows/cd_tests.yml) | `push` в `tests/**` | тестовая сборка + раздача в Firebase App Distribution |
| [`cd_release.yml`](.github/workflows/cd_release.yml) | `push` в `releases/**`, имя обязано заканчиваться на `v.X.Y.Z` | подписанные APK + AAB, публикация в Play, отчёт в Telegram |

```bash
# запустить релизный пайплайн версии 1.2.3
git push origin HEAD:releases/v.1.2.3
```

---

## Gradle: флаги и диагностика

| Команда / флаг | Зачем |
|----------------|-------|
| `./gradlew --stop` | остановить демонов (после смены JDK, при странных ошибках) |
| `./gradlew clean` | удалить `build/` |
| `--continue` | не останавливаться на первой ошибке (удобно для `ktlintCheck` / `detekt`) |
| `--offline` | сборка без обращения к сети |
| `--no-configuration-cache` | configuration cache включён в `gradle.properties`; отключить, если плагин с ним не дружит (Ruler) |
| `--rerun-tasks` | игнорировать up-to-date и build cache |
| `--refresh-dependencies` | перекачать зависимости |
| `--profile` | HTML-профиль сборки |
| `-Pmodules.graph.output.gv=…` | путь вывода DOT-графа модулей |
| `-PversionName=` / `-PversionCode=` | версия сборки (читаются в `AndroidAppConventionPlugin`) |
| `./gradlew tasks --all` | все задачи проекта |
| `./gradlew :app:tasks` | задачи одного модуля, сгруппированные |
| `./gradlew help --task <task>` | описание задачи и в каких модулях она есть |
| `./gradlew properties` | значения проектных свойств |
| `./gradlew buildEnvironment` | JDK демона и зависимости buildscript (версии плагинов) |
| `./gradlew :app:kotlinDslAccessorsReport` | доступные type-safe аксессоры Kotlin DSL |

Версии всех библиотек и плагинов — в [`gradle/libs.versions.toml`](gradle/libs.versions.toml),
конвеншен-плагины `soft.divan.*` — в [`build-logic/`](build-logic/).

---

## Если что-то сломалось

```bash
./gradlew --stop                                   # 1. прибить демонов
./gradlew <task> --no-configuration-cache          # 2. исключить configuration cache
./gradlew clean <task>                             # 3. чистая сборка модуля/проекта
./gradlew <task> --refresh-dependencies            # 4. перекачать зависимости
rm -rf .gradle build */build                       # 5. локальный кэш проекта
```

Куда смотреть при падении:

| Симптом | Отчёт |
|---------|-------|
| «Configuration cache state could not be cached» | `build/reports/configuration-cache/**/configuration-cache-report.html` |
| Любые предупреждения/проблемы сборки | `build/reports/problems/problems-report.html` |
| Падение R8 в release | `app/build/outputs/mapping/release/missing_rules.txt` |
| Room ругается на identity hash | версия `@Database` должна быть **строго больше** `user_version` ассета — см. [«База данных»](#база-данных) |

---

## Эмулятор

`emulator` не лежит в `PATH` — он в Android SDK (`sdk.dir` из `local.properties`,
на macOS по умолчанию `~/Library/Android/sdk`):

```bash
EMU=~/Library/Android/sdk/emulator/emulator
$EMU -list-avds                                    # какие AVD есть
$EMU -avd Pixel_7 -no-snapshot-save -no-boot-anim & # запустить в фоне
adb wait-for-device                                 # дождаться подключения
adb shell getprop sys.boot_completed                # "1" = система загрузилась
adb emu kill                                        # выключить
```

---

## ADB: отладка на устройстве

Ниже подразумевается debug-сборка; для release замените пакет на `soft.divan.financemanager`.

```bash
PKG=soft.divan.financemanager.debug
```

### Установка и запуск

```bash
adb devices -l                                 # список устройств
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n $PKG/soft.divan.financemanager.presenter.MainActivity
adb shell am force-stop $PKG
adb shell pm clear $PKG                        # сбросить данные (БД, DataStore, PIN)
adb shell dumpsys package $PKG | grep -E "versionName|versionCode"
adb uninstall $PKG
```

### Логи

```bash
adb logcat -c                                            # очистить буфер
adb logcat --pid=$(adb shell pidof -s $PKG)              # только наш процесс
adb logcat --pid=$(adb shell pidof -s $PKG) "*:E"        # только ошибки
adb logcat -d -s SyncWorker WM-WorkerWrapper             # по тегам, разово
```

> В zsh шаблон приоритета обязательно в кавычках (`"*:E"`), иначе шелл попытается развернуть `*`.

### Данные приложения

```bash
adb shell run-as $PKG ls databases              # finance_manager_db.db (+ -wal, -shm)
adb shell run-as $PKG ls files/datastore        # session/token/sync/haptics/languages/currency…
adb shell run-as $PKG ls shared_prefs           # хранилище хэша PIN (feature:security)
```

Файлы создаются лениво — БД появится только после того, как экран реально к ней обратится,
а `*_preferences.preferences_pb` — после первой записи в соответствующий DataStore.

Секреты (JWT, хэш PIN) зашифрованы `CryptoManager` (AES/GCM + KeyStore) — в открытом виде
из дампа их не достать, это ожидаемо.

### Синхронизация и WorkManager

Самый быстрый способ увидеть очередь работ — встроенная диагностика WorkManager:

```bash
adb logcat -c
adb shell am broadcast -a "androidx.work.diagnostics.REQUEST_DIAGNOSTICS" -p $PKG
adb logcat -d -s WM-DiagnosticsWrkr
```

Выведет таблицу с `Job Id`, `State` и `Unique Name` — у нас это `SyncPeriodicWork`,
`SyncOneTimeWork`, `InactivityNotificationWork`.

Принудительно запустить работу по её `Job Id` (например `10`):

```bash
adb shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler $PKG 10
adb logcat -d -s WM-WorkerWrapper SyncWorker
```

> ⚠️ `-n androidx.work.systemjobscheduler` обязателен: WorkManager регистрирует джобы в своём
> namespace, и без флага команда ответит `Could not find job N`. `Job Id` меняется при
> переустановке — сверяйтесь с диагностикой.

Проверить поведение в Doze:

```bash
adb shell dumpsys deviceidle force-idle
adb shell dumpsys deviceidle unforce
```

Проверить офлайн-путь (проект offline-first — это штатный сценарий):

```bash
adb shell svc wifi disable && adb shell svc data disable
adb shell svc wifi enable  && adb shell svc data enable
```

### Уведомления

Канал: `finance_manager_general` (создаётся лениво — при первом уведомлении).

```bash
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS      # API 33+, без диалога
adb shell dumpsys package $PKG | grep POST_NOTIFICATIONS           # granted=true?
adb shell dumpsys notification --noredact | grep -A4 finance_manager_general
```

Чтобы увидеть напоминание о неактивности, не дожидаясь таймера, — запустите
`InactivityNotificationWork` через `cmd jobscheduler run` (см. выше).

### UI: тема, язык, шрифт, скриншоты

```bash
adb shell cmd uimode night yes                                  # тёмная тема (night no — обратно)
adb shell cmd locale set-app-locales $PKG --locales ru-RU        # язык приложения (API 33+)
adb shell cmd locale get-app-locales $PKG
adb shell cmd locale set-app-locales $PKG --locales ""           # сбросить на системный
adb shell settings put system font_scale 1.3                     # масштаб шрифта (1.0 — обратно)
adb exec-out screencap -p > screen.png                           # скриншот
adb shell screenrecord /sdcard/demo.mp4                          # запись экрана (Ctrl+C — стоп)
adb pull /sdcard/demo.mp4
```

📖 [docs/synchronization.md](docs/synchronization.md), [docs/notifications.md](docs/notifications.md)

---

## База данных

БД — `finance_manager_db.db`, создаётся из ассета `app/src/main/assets/database/category_db.db`.
Удобнее всего смотреть через **App Inspection → Database Inspector** в Android Studio, но можно
и вытащить файл (debug-сборка отлаживаемая, поэтому `run-as` работает):

```bash
PKG=soft.divan.financemanager.debug
adb exec-out run-as $PKG cat databases/finance_manager_db.db     > fm.db
adb exec-out run-as $PKG cat databases/finance_manager_db.db-wal > fm.db-wal
sqlite3 fm.db ".tables"                    # account categories currency transactions …
sqlite3 fm.db "PRAGMA user_version;"       # должно совпасть с version в @Database
sqlite3 fm.db "select count(*) from transactions;"
```

Проверка ключевого инварианта проекта — версия `@Database` обязана быть **строго больше**
`user_version` прешипнутого ассета, иначе Room падает на несовпадении identity hash:

```bash
sqlite3 app/src/main/assets/database/category_db.db "PRAGMA user_version;"   # сейчас 1
grep -n "version = " core/database/src/main/java/soft/divan/financemanager/core/database/db/FinanceManagerDatabase.kt
```

📖 [docs/bd.md](docs/bd.md)

---

## Где лежат отчёты

| Отчёт | Путь |
|-------|------|
| Покрытие (HTML) | `build/reports/kover/htmlFull/index.html` |
| Покрытие (XML) | `build/reports/kover/reportFull.xml` |
| Detekt | `build/reports/detekt/detekt.html` (+ `.xml`, `.sarif`, `.md`) |
| Ktlint | `<модуль>/build/reports/ktlint/**` |
| Android Lint | `<модуль>/build/reports/lint-results-debug.html` |
| Юнит-тесты | `<модуль>/build/reports/tests/testDebugUnitTest/index.html` |
| Размер приложения (Ruler) | `app/build/reports/ruler/debug/report.html` |
| Время сборки | `app/build/reports/buildTimeTracker/build.csv` |
| Профиль сборки (`--profile`) | `build/reports/profile/profile-<дата>.html` |
| Проблемы configuration cache | `build/reports/configuration-cache/**/configuration-cache-report.html` |
| Проблемы сборки (Gradle) | `build/reports/problems/problems-report.html` |
| Правила, которых не хватило R8 | `app/build/outputs/mapping/release/missing_rules.txt` |
| Карта навигации | `app/build/navgraph/`, `app/build/navgallery/`, закоммиченная копия — `docs/graphs/nav_graph/` |
| APK / AAB | `app/build/outputs/apk/`, `app/build/outputs/bundle/` |

---

## Куда смотреть дальше

- [docs/README.md](docs/README.md) — оглавление всей документации
- [CLAUDE.md](CLAUDE.md) / [AGENTS.md](AGENTS.md) — правила работы с кодовой базой
- [TODO.md](TODO.md) — технический бэклог
- [docs/ci-cd.md](docs/ci-cd.md) — что и как проверяет CI, какие есть секреты
