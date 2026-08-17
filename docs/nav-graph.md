# Карта навигации (compose-nav-graph)

Граф экранов Finance Manager разложен по 17 модулям `feature:*:impl`: каждая фича
регистрирует свои `NavEntry` сама, а переходы к соседям идут через `Navigator.goTo(ЕёKey)`
(см. [`navigation3.md`](./navigation3.md)). Плюс такой изоляции — модули не знают друг о
друге; минус — **целиком граф не виден нигде**: чтобы понять, откуда открывается
`TransactionKey`, приходится грепать по `goTo` во всех `:impl`.

[`skydoves/compose-nav-graph`](https://github.com/skydoves/compose-nav-graph) закрывает
именно эту дыру: он собирает граф **статически, на этапе сборки** — по аннотациям в коде,
без запуска приложения и эмулятора — и отдаёт его в виде JSON, интерактивного HTML, PNG и
панели в Android Studio, где каждый экран показан своим отрендеренным `@Preview`.

## Как это устроено

Инструмент состоит из четырёх частей, каждая делает свой кусок работы:

| Компонент | Артефакт | Что делает |
| --- | --- | --- |
| Аннотации | `com.github.skydoves:compose-nav-graph-annotations` | `@NavDestination`, `@NavEdge`, `@NavGraphRoot`, `@NavPreview` — описание графа прямо в коде экрана |
| KSP-процессор | `com.github.skydoves:compose-nav-graph-ksp` | читает аннотации модуля и пишет `nav-graph.json` в ресурсы KSP |
| Gradle-плагин | `com.github.skydoves.navgraph` | подключает первые два, рендерит превью через Layoutlib, мержит графы модулей, даёт задачи `generateNavGraph` / `navDump` / экспорт |
| IDE-плагин | «Compose Navigation Graph» из JetBrains Marketplace | рисует итоговый граф и галерею превью в окне **View → Tool Windows → NavGraph Graph** |

Аннотации и процессор в модули **не прописываются руками** — их добавляет сам Gradle-плагин
(`autoDependencies`, по умолчанию включено).

### Конвейер сборки

```mermaid
flowchart LR
    subgraph Модуль["feature:*:impl (×17) и :app"]
        SRC["Экраны с @NavDestination /<br/>@NavEdge / @NavPreview"]
        KSP["kspDebugKotlin<br/>(compose-nav-graph-ksp)"]
        JSON["nav-graph.json<br/>узлы, рёбра, аргументы"]
        RENDER["renderNavGraphLayoutlib<br/>рендер @NavPreview"]
        PNG["thumbs/*.png"]
        MERGE["mergeNavGraph<br/>граф + превью"]
        SRC --> KSP --> JSON --> MERGE
        SRC --> RENDER --> PNG --> MERGE
    end

    MERGE -->|"конфигурация<br/>navgraphGraphElements"| AGG

    subgraph App[":app"]
        AGG["aggregateNavGraph"]
        OUT["nav-graph.json<br/>21 узел, 22 ребра"]
        HTML["nav-graph.html / .png"]
        IDE["панель NavGraph Graph"]
        AGG --> OUT --> HTML
        OUT --> IDE
    end
```

Ключевое: **межмодульных зависимостей ради графа не появляется**. Каждый модуль публикует
свой `nav-graph.json` в Gradle-конфигурацию `navgraphGraphElements`, а `:app` забирает
артефакты по уже существующим `implementation`-зависимостям и склеивает их. Модуль,
у которого нет аннотаций, отдаёт пустой граф.

### Откуда берутся узлы и рёбра

Процессор ищет типы, реализующие `androidx.navigation3.runtime.NavKey` (если `NavKey` нет
на classpath — молча отдаёт пустой граф), и строит:

- **узел** — из `@NavDestination(route = XKey::class)` на `@Composable`-функции. В узел
  попадают имя маршрута, модуль, FQN composable-функции, файл и строка (по ним IDE делает
  переход в код) и **аргументы**, прочитанные из свойств самого ключа
  (`TransactionKey(isIncome: Boolean, transactionId: String?)`);
- **ребро** — из `@NavEdge(to = ...)`. Источник (`from`) выводится из `@NavDestination` на
  той же функции; если ребро принадлежит не экрану, а хосту — `from` задаётся явно;
- **стартовый экран** — из `@NavGraphRoot`;
- **превью** — из `@NavPreview(route = ...)` рядом с обычным `@Preview`. Layoutlib (тот же
  движок, что рисует превью в Android Studio) рендерит их в PNG на этапе сборки.

Узел, на который есть ребро, но нет `@NavDestination`, всё равно появится в графе. Такой узел
может быть с превью или без: `@NavPreview(route = XKey::class)` привязывает миниатюру к
маршруту **независимо** от `@NavDestination`. Так сделан `ProfileAuthKey` — это тот же
`AuthScreen`, поэтому своего `@NavDestination` у него нет, но миниатюра есть (см. раздел
«Превью и миниатюры»).

## Как размечен наш проект

Аннотации живут **на экранах в `:impl`**, а не на ключах в `:api`. Это не случайность:
ребро `HistoryKey → TransactionKey` требует видеть оба ключа, и у `:impl` такая зависимость
уже есть (он и так вызывает `goTo(TransactionKey(...))`), а у `:api` её нет — разметка в
`:api` потянула бы за собой новые связи между модулями и сломала `assertModuleGraph`.

```kotlin
// feature/history/impl/.../HistoryScreen.kt
@NavDestination(route = HistoryKey::class)
@NavEdge(to = TransactionKey::class, label = "операция")
@NavEdge(to = AnalysisKey::class, label = "анализ")
@Composable
fun HistoryScreen(...)

@NavPreview(route = HistoryKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HistoryScreenPreview() { ... }
```

Переходы корневого стека (`RootNavDisplay`) принадлежат не экрану, а хосту — там `from`
указан явно:

```kotlin
// app/.../RootNavDisplay.kt
@NavEdge(from = SplashKey::class, to = AuthKey::class, label = "нет сессии")
@NavEdge(from = SplashKey::class, to = MainKey::class, label = "есть сессия")
@NavEdge(from = AuthKey::class, to = MainKey::class, label = "вход выполнен")
@Composable
fun RootNavDisplay(...)
```

### Что получилось

**21 узел и 22 ребра из 17 модулей, все 21 узла — с отрендеренным превью.**

Сгенерированная карта (миниатюры — реальные экраны, отрисованные Layoutlib без эмулятора):

![Карта навигации Finance Manager](graphs/nav_graph/nav-graph.png)

Интерактивная версия — [`graphs/nav_graph/nav-graph.html`](graphs/nav_graph/nav-graph.html):
самодостаточный файл (миниатюры вшиты в base64), открывается в браузере, узлы кликабельны.

Рядом лежит **галерея всех `@Preview` проекта**, сгруппированная по модулям и пакетам:
[`graphs/nav_graph/preview-gallery.html`](graphs/nav_graph/preview-gallery.html)
(![png](graphs/nav_graph/preview-gallery.png) — статичная версия).

Все четыре файла (`nav-graph.png` / `nav-graph.html` / `preview-gallery.png` /
`preview-gallery.html`) лежат в `docs/graphs/nav_graph/` и обновляются одной командой
`./gradlew :app:exportNavGraphToDocs` (см. [«Команды»](#команды)).

Схема тех же переходов в текстовом виде:

```mermaid
flowchart TD
    Splash["SplashKey ★"]
    Auth[AuthKey]
    Main[MainKey]

    Splash -->|нет сессии| Auth
    Splash -->|есть сессия| Main
    Auth -->|вход выполнен| Main

    Main -->|вкладка «Расходы» / «Доходы»| Today["TransactionsTodayKey<br/>isIncome"]
    Main -->|вкладка «Счёт»| MyAccounts[MyAccountsKey]
    Main -->|вкладка «Статьи»| Category[CategoryKey]
    Main -->|вкладка «Настройки»| Settings[SettingsKey]

    Today -->|история| History["HistoryKey<br/>isIncome"]
    Today -->|новая / существующая операция| Transaction["TransactionKey<br/>isIncome, transactionId"]
    History -->|операция| Transaction
    History -->|анализ| Analysis["AnalysisKey<br/>isIncome"]
    MyAccounts -->|создать / изменить счёт| Account["AccountKey<br/>accountId"]

    Settings -->|о программе| About[AboutTheProgramKey]
    Settings -->|безопасность| Security[SecurityKey]
    Settings -->|оформление| Design[DesignAppKey]
    Settings -->|хаптик| Haptics[HapticsKey]
    Settings -->|звуки| Sounds[SoundsKey]
    Settings -->|язык| Languages[LanguagesKey]
    Settings -->|синхронизация| Sync[SynchronizationKey]
    Settings -->|профиль| Profile[ProfileKey]

    Security -->|установить PIN| CreatePin[CreatePinKey]
    Profile -->|сменить аккаунт| ProfileAuth[ProfileAuthKey]
```

Граф читается и как проверка архитектуры: видно, что `SettingsKey` — единственная точка
входа в восемь экранов-настроек, что `TransactionKey` открывается из двух мест, и что
вкладки нижней навигации — это четыре независимых поддерева под `MainKey`.

Два места читаются неочевидно — это не баг рендера, а как плагин моделирует граф:

- **`MainKey` — это оболочка с нижней навигацией, а не экран.** Её миниатюра — намеренная
  заглушка (`MainScreenPreview`): в превью нельзя поднять реальный граф вкладок (он идёт
  через DI/навигацию), поэтому рисуется нижний бар + подпись выбранной вкладки («Expenses»).
  Реальный контент вкладок — это **отдельные узлы**: `TransactionsTodayKey`, `MyAccountsKey`,
  `CategoryKey`, `SettingsKey` (сама «Expenses today» со списком операций — узел
  `TransactionsTodayKey`, а не `MainKey`).
- **«Расходы» и «Доходы» — это один узел `TransactionsTodayKey`, а не два.** Обе вкладки —
  один и тот же экран `TransactionsTodayScreen` с аргументом `isIncome: Boolean` (Расходы =
  `isIncome=false`, Доходы = `isIncome=true`). Плагин моделирует **один маршрут-с-аргументом
  как один узел**, поэтому отдельного «Income» в графе нет — оба таба ведут в
  `TransactionsTodayKey`, и это отражено в подписи ребра `MainKey → TransactionsTodayKey`
  («вкладка «Расходы» / «Доходы»»). Так же устроены `HistoryKey`/`AnalysisKey`/`TransactionKey`
  — у них тоже `isIncome` в аргументах.

## Превью и миниатюры

Миниатюра узла — это `@Preview`, помеченный `@NavPreview(route = XKey::class)` и отрисованный
Layoutlib **headless**, без эмулятора. Отсюда главное правило: **превью должно быть
DI-free** — рендер идёт без запущенного Hilt-графа, поэтому реальную функцию-экран
`XScreen()` (она тянет `hiltViewModel()`) рисовать нельзя, она упадёт. У нас превью
вызывают внутренний `XContent(uiState = mock..., ...)` с mock-состоянием — это и есть
«representative preview» из гайда: выглядит как экран, но не лезет наружу за Compose.

Сейчас миниатюры есть у **всех 21 узла**. Три случая потребовали ручной доводки:

- **`ProfileScreen` и `CreatePinScreen`** — у экранов не было `@Preview` вообще. Добавили
  DI-free превью: `ProfileScreen` рисует `ProfileContent` в состоянии авторизованного
  пользователя, `CreatePinScreen` — экран ввода PIN.
- **`ProfileAuthKey`** — у него нет своего `@NavDestination` (это тот же `AuthScreen`).
  Миниатюру дали отдельным `@NavPreview(route = ProfileAuthKey::class)` на превью, которое
  рисует `AuthContent`. `@NavPreview` привязывается к маршруту независимо от `@NavDestination`.
- **`CreatePinScreen`** — его настоящий `PinEntryCommonScreen` принимает параметр типа
  `android.hardware.biometrics.BiometricPrompt.AuthenticationCallback`, а этот класс не
  загружается в headless-Layoutlib (`NoClassDefFoundError`). Поэтому превью собрано из тех
  же под-компонентов напрямую (`PinCodeScreenHeader` + `RoundedBoxesRow` + `Keyboard`), без
  биометрического параметра.
- **`PreviewPinLockScreen`** (галерея) — вынесен в отдельный файл `PinLockScreenPreview.kt`.
  Важное правило: **все top-level функции одного `.kt` компилируются в один класс**, а
  `ComposableInvoker` ищет превью через `getDeclaredMethods()`, который резолвит типы
  параметров *всех* методов класса. Поэтому `@Preview` нельзя держать в одном файле с
  функцией, чья сигнатура ссылается на недоступный в headless-рендере класс (здесь —
  `BiometricPrompt` в `PinLockScreenContent`): падает весь превью, даже если сам он этот тип
  не трогает. Лечится выносом `@Preview` в отдельный файл без такой ссылки.

Две миниатюры Layoutlib рисует не полностью — это ограничение движка рендера, а не ошибка
разметки:

| Экран | Что видно | Почему |
| --- | --- | --- |
| `SplashKey` | пустой фон | экран — это Lottie-анимация (`LottieAnimation`), а Lottie в headless-Layoutlib не воспроизводится |
| `AnalysisKey` | период, сумма, легенда категорий — но без самой круговой диаграммы | `PieChart` из YCharts рисуется на кастомном `Canvas`, который Layoutlib не отрисовывает |

Превью `AnalysisScreen` при этом специально переведено с состояния ошибки на
`mockTransactionUiStateSuccess` — так миниатюра показывает реальную структуру экрана, а не
экран ошибки.

## Команды

Все задачи регистрируются в каждом модуле с плагином; в `:app` они работают с
агрегированным графом.

| Команда | Что делает |
| --- | --- |
| `./gradlew :app:aggregateNavGraph` | склеивает графы всех модулей → `app/build/navgraph-aggregated/nav-graph.json` |
| `./gradlew :app:generateNavGraph` | граф одного модуля + рендер превью → `app/build/navgraph/` |
| `./gradlew :app:exportNavGraphHtml` | интерактивный HTML со всеми экранами и переходами → `app/build/navgraph/` |
| `./gradlew :app:exportNavGraphImage` | тот же граф одной PNG-картинкой → `app/build/navgraph/` |
| **`./gradlew :app:exportNavGraphToDocs`** | **экспортирует граф и галерею превью (PNG + HTML) сразу в `docs/graphs/nav_graph/`** — коммитим как документацию (задача в `app/build.gradle.kts`) |
| `./gradlew :app:generatePreviewGallery` | рендер всех `@Preview` проекта (галерея превью в IDE) |
| `./gradlew :app:exportPreviewGalleryHtml` | галерея превью отдельным HTML → `app/build/navgallery/` |
| `./gradlew :app:exportPreviewGalleryImage` | галерея превью одной PNG → `app/build/navgallery/` |
| `./gradlew navDump` | перезаписать `.nav`-бейзлайны во всех модулях |
| `./gradlew navCheck` | проверить, что граф не разошёлся с бейзлайнами |

Обновить закоммиченную карту и галерею в `docs/graphs/nav_graph/` после изменения графа:

```bash
./gradlew :app:exportNavGraphToDocs
```

### Бейзлайн `.nav`

Рядом с каждым размеченным модулем лежит закоммиченный слепок его графа —
`<модуль>/nav/*.nav` (по аналогии с `apiDump` / `apiCheck` в binary-compatibility-validator):

```
# feature/settings/impl/nav/impl.nav
dest SettingsKey
dest SecurityKey
...
edge SettingsKey -> SecurityKey  "безопасность"
```

Смысл — сделать изменение навигации **видимым в диффе ревью**: добавили переход
«настройки → новый экран» — в PR появляется строка `edge`. Файл генерируется, руками его
не правят: после осознанного изменения графа делают `./gradlew navDump` и коммитят результат.
`navCheck` падает при расхождении (`failOnNavChange = true`) и при отсутствии бейзлайна у
нового модуля.

`navCheck` **не входит** в `check` и пока не подключён к CI — при желании это отдельная
джоба в [`ci.yml`](../.github/workflows/ci.yml) рядом с `assertModuleGraph`.

## Подключение в проекте

Плагин подключается не в build-файлах модулей, а через convention-плагины — как и всё
остальное в проекте:

- `soft.divan.feature.impl` и `soft.divan.android.app` вызывают
  `Project.configureNavGraph()` (`build-logic/.../soft/divan/financemanager/NavGraph.kt`);
- версия — в `libs.versions.toml` (`navgraph = "0.2.1"`), сам плагин объявлен в корневом
  `build.gradle.kts` c `apply false`;
- `:api`-модули плагин **не получают**: ключи там объявлены, но аннотированных
  composable-функций нет, а аргументы узлов процессор читает с classpath.

### Настройки и их последствия

```kotlin
extensions.configure<NavGraphExtension> {
    renderBackend.set(RenderBackend.LAYOUTLIB)
    galleryRenderBackend.set(RenderBackend.LAYOUTLIB)
}
```

Бэкенд рендера зафиксирован на Layoutlib осознанно. При значении по умолчанию (`AUTO`)
плагин дополнительно генерирует в каждый модуль Robolectric-тест
`NavGraphRobolectricRenderTest`, который попадает в `testDebugUnitTest` — то есть рендер
картинок начинает выполняться в CI-джобе с юнит-тестами (и падать там).

Второй побочный эффект убрать нельзя: плагин **принудительно включает**
`testOptions.unitTests.isIncludeAndroidResources` каждому Android-модулю — рендереру нужен
`apk_for_local_test` со скомпилированными ресурсами. Без этого превью просто не рисуются
(`resourceApkPath` пустой → `FileNotFoundException`). Из-за этого пришлось поправить три
места:

| Что сломалось | Почему | Как починено |
| --- | --- | --- |
| `:feature:auth:impl:processDebugUnitTestManifest` | AGP начал собирать манифест unit-тестов и мержить в него манифест Yandex ID SDK с `${YANDEX_CLIENT_ID}` | заглушки-плейсхолдеры в `feature/auth/impl/build.gradle.kts`; реальное значение по-прежнему подставляет `:app` |
| `:feature:synchronization:impl:processDebugUnitTestResources` | `ic_sync_notification.xml` из `:sync` ссылается на `?attr/colorControlNormal`, а appcompat доезжал только транзитивно через `:app` | `:sync` объявил зависимость на appcompat явно |
| `:feature:splash-screen:impl:testDebugUnitTest` | задача перестала быть `NO-SOURCE`, и Gradle 9 упал на `failOnNoDiscoveredTests` в модуле без единого теста | проверка отключается только в модулях без `src/test` (в `configureNavGraph`) |

## Как размечать новый экран

1. Экран регистрируется в `<Name>FeatureImpl.registerEntries` как обычно.
2. На `@Composable`-функцию экрана — `@NavDestination(route = XKey::class)`.
3. На неё же — по одному `@NavEdge(to = ..., label = "...")` на каждый переход, который
   экран умеет делать (аннотация повторяемая). Метка — по-русски, как в UI.
4. На основной `@Preview` — `@NavPreview(route = XKey::class, primary = true)`; остальные
   превью того же экрана можно пометить без `primary` (так сделано у `AuthScreen` — пять
   состояний). Превью должно быть **DI-free** (рисовать `XContent(mock)`, а не `XScreen()`) —
   иначе headless-рендер упадёт. Подробнее — раздел «Превью и миниатюры».
5. `./gradlew navDump` и закоммитить обновлённый `.nav`.
6. `./gradlew :app:exportNavGraphToDocs` — обновить карту и галерею в
   `docs/graphs/nav_graph/` и закоммитить их вместе с изменениями.

Ключ, у которого нет `@NavDestination`, попадёт в граф пустой карточкой — это сигнал, что
экран не размечен.

## Ограничения

- **Один экран — один `@NavDestination`.** `AuthScreen` обслуживает и `AuthKey`, и
  `ProfileAuthKey`, но `@NavDestination` вешается только один раз (на `AuthKey`). Миниатюру
  `ProfileAuthKey` при этом дали отдельным `@NavPreview` (см. «Превью и миниатюры»).
- **Headless-рендер рисует не всё.** Lottie (`SplashKey`) и кастомный `Canvas` YCharts
  (диаграмма `AnalysisKey`) Layoutlib не отрисовывает; типы из `android.hardware.*` в превью
  приводят к `NoClassDefFoundError`. Обход — DI-free representative-превью из простых
  Compose-компонентов (см. «Превью и миниатюры»).
- **Рендер галереи бывает flaky.** Превью модуля рисуются одним процессом с таймаутом на
  «зависание»: если превью стопорится дольше таймаута, рендерер убивают, и **оставшиеся
  превью модуля выходят как «no preview»** (на медленном CI-раннере так может отвалиться
  целый модуль, хотя локально рендерится). Это не ошибка разметки — перезапуск
  `exportPreviewGalleryImage`/`Html` обычно всё дорисовывает.
- **Граф статический.** Условные переходы (`if (authStatus == UNAUTHORIZED)`) видны как
  два ребра с метками, но самого условия в графе нет; рёбра, не описанные аннотацией,
  в граф не попадут — разметку нужно поддерживать руками (для этого и `navCheck`).
- **Рендер требует Layoutlib и JDK 21.** Первый запуск скачивает `layoutlib` и
  `compose-preview-renderer` (~100 МБ) в кэш Gradle. Важно: `Bridge` из Layoutlib `16.2.1`
  скомпилирован под **Java 21** (class file version 65), поэтому Gradle-JVM для рендера
  обязан быть **JDK 21** — на JDK 17 рендер падает с `UnsupportedClassVersionError`, и **все**
  превью выходят «no preview» (граф и рёбра при этом строятся корректно — их даёт KSP, а он на
  17 работает). Локальная разработка идёт на JDK 21; в CI джоба `nav-graph` (`.github/workflows/ci.yml`)
  явно поднимает JDK 21 отдельным шагом, т.к. общий `android-setup` ставит 17.
- Плагин относительно новый (0.2.1, июль 2026) и на сборку не влияет: ни одна его задача
  не входит в `assembleDebug`, `check` или `testDebugUnitTest`.

## Ключевые файлы

| Файл | Роль |
| --- | --- |
| `build-logic/.../soft/divan/financemanager/NavGraph.kt` | подключение и настройка плагина |
| `build-logic/.../FeatureImplConventionPlugin.kt` | плагин для всех `feature:*:impl` |
| `build-logic/.../AndroidAppConventionPlugin.kt` | плагин для `:app` (агрегация) |
| `app/build.gradle.kts` | задача `exportNavGraphToDocs` (граф + галерея → `docs/graphs/nav_graph/`) |
| `docs/graphs/nav_graph/nav-graph.{png,html}` | закоммиченная карта навигации для документации |
| `docs/graphs/nav_graph/preview-gallery.{png,html}` | закоммиченная галерея всех `@Preview` |
| `gradle/libs.versions.toml` | версия `navgraph` и артефакт для convention-плагинов |
| `feature/*/impl/.../*Screen.kt` | `@NavDestination` / `@NavEdge` / `@NavPreview` |
| `app/.../presenter/navigation/RootNavDisplay.kt` | рёбра корневого стека (явный `from`) |
| `app/.../presenter/screens/MainScreen.kt` | узел `MainKey` и рёбра вкладок |
| `*/nav/*.nav` | закоммиченные бейзлайны графа |
| `app/build/navgraph-aggregated/nav-graph.json` | итоговый граф (генерируется) |
