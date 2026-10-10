# CI/CD

Документ описывает, как в **Finance Manager** устроены непрерывная интеграция и доставка:
какие workflow есть, чем они триггерятся, что именно проверяют, какие артефакты и отчёты
производят и какие секреты для этого нужны. Что ещё не сделано — в «Связанных задачах» в конце.

Всё построено на **GitHub Actions**. Логика намеренно тонкая: workflow лишь дёргают Gradle,
а вся «умная» часть (пороги покрытия, правила статического анализа, проверка архитектуры,
подпись, версии) живёт в сборке — в корневом `build.gradle.kts` и конвеншен-плагинах
`build-logic`. Повторяющиеся шаги вынесены в **composite actions** (`.github/actions/*`).

## Общая картина

```mermaid
flowchart TD
    pr["pull_request<br/>(любая ветка)"] --> ci["ci.yml — CI<br/>11 параллельных джоб"]
    pushM["push в master / releases/**"] --> ci
    pr --> sec["security.yml<br/>gitleaks + dependency-review"]
    pushM --> sec
    pushM --> dep["dependency-submission.yml<br/>граф зависимостей → GitHub"]
    pushT["push в tests/**"] --> cdt["cd_tests.yml — App test"]
    pushR["push в releases/**"] --> cdr["cd_release.yml — App release"]
    wd["workflow_dispatch"] -.-> cdt
    wd -.-> cdr

    ci --> art1["APK debug, отчёты:<br/>Kover, Lint, Detekt, KtLint,<br/>Ruler, граф модулей, build time,<br/>dependency analysis, карта навигации"]
    sec --> art4["Алерты о секретах и<br/>уязвимых зависимостях"]
    dep --> art5["Dependabot alerts,<br/>основа для dependency-review"]
    cdt --> art2["APK debug →<br/>Telegram + Firebase App Distribution"]
    cdr --> art3["APK + AAB (signed) →<br/>Telegram + Google Play (internal, draft)<br/>+ черновик GitHub Release"]
```

Workflow разделены по назначению:

| Workflow | Файл | Назначение |
|---|---|---|
| **CI** | [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) | Гейт качества: сборка, тесты, покрытие, статический анализ, размер приложения, граф модулей, бейзлайн графа навигации, здоровье зависимостей. |
| **Security** | [`.github/workflows/security.yml`](../.github/workflows/security.yml) | Поиск утёкших секретов (gitleaks) и уязвимых зависимостей в PR (dependency-review). |
| **Dependency submission** | [`.github/workflows/dependency-submission.yml`](../.github/workflows/dependency-submission.yml) | Отдаёт GitHub граф зависимостей — без него не работают Dependabot alerts. APK-classpath помечен scope `runtime`, build-tooling — `development` (см. [Dependency vulnerabilities](./dependency-vulnerabilities.md)). |
| **Board** | [`.github/workflows/board.yml`](../.github/workflows/board.yml) | Доска задач: ветка `feature/FM-N-…` → In Progress, PR → In Review, недельная сводка в Telegram. См. [Доска задач](./task-tracking.md). |
| **CI failure** | [`.github/workflows/ci-failure.yml`](../.github/workflows/ci-failure.yml) | Красный `master` → issue с лейблом `ci-failure`; зелёный прогон закрывает его. |
| **App test** | [`.github/workflows/cd_tests.yml`](../.github/workflows/cd_tests.yml) | Доставка тестовой (debug) сборки тестировщикам. |
| **App release** | [`.github/workflows/cd_release.yml`](../.github/workflows/cd_release.yml) | Подписанный релиз: APK + AAB, публикация в Google Play, черновик GitHub Release. |

Обновление зависимостей автоматизировано **Renovate** ([`.github/renovate.json5`](../.github/renovate.json5)):
он читает `gradle/libs.versions.toml`, `gradle-wrapper.properties` и версии actions,
группирует связанные обновления (Kotlin + KSP + Hilt, AndroidX Compose, Firebase) и
раз в неделю открывает PR. Патчи инструментов статического анализа и минорные обновления
actions мержатся автоматически, major — только через Dependency Dashboard.
Требует установки GitHub App **Mend Renovate** на репозиторий.

AI-ревью PR — **CodeRabbit** (бесплатен для публичных репозиториев). Это GitHub App, а не
workflow: после установки приложения он ревьюит каждый PR, кроме черновиков, и дообновляет
ревью на каждый push. PR Renovate тоже ревьюятся — по отдельному правилу для обновлений версий
(breaking changes, связанные версии, известные ограничения проекта); для этого в панели
CodeRabbit пользователю `renovate[bot]` выдан seat. Ревью рекомендательное — мерж не
блокирует, гейт качества остаётся за CI. Автомерж Renovate дожидается окончания ревью
(статус CodeRabbit), но замечания бота его не останавливают.
Правила разложены по трём уровням:

| Где | Что |
|---|---|
| [`.coderabbit.yaml`](../.coderabbit.yaml) | Настройки бота, формат сводки в описании PR (что сделано, зачем, затронутые модули, тесты, риски), правила по областям кода (`path_instructions`) и pre-merge проверки: версия БД при изменении схемы, оформление нового модуля, пары строк `values` / `values-ru`. `.nav`-бейзлайн сюда не входит — его проверяет `navCheck` в CI. |
| [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) | Общие правила ревью: шкала важности, что не комментировать, осознанные решения, инварианты. Читается и CodeRabbit, и Copilot. |
| [`docs/agents/*.md`](./agents/) | Подробные гайды по областям — подключены как code guidelines. |

Инструменты бота, которые дублировали бы CI, выключены: detekt и gitleaks (работают в CI) и
osvScanner (уязвимые зависимости в CI ловит dependency-review). markdownlint, languagetool и
yamllint тоже выключены — шумят на русскоязычной документации и длинных строках YAML.
Включены actionlint, zizmor и shellcheck — workflow и скрипты в CI больше никто не проверяет.
В текстах инструкций `.coderabbit.yaml` нельзя использовать
обратные кавычки, `${…}` и обратные слэши — CodeRabbit на них ломается.

Общие настройки во всех workflow, запускающих Gradle:

```yaml
env:
  gradleFlags: --parallel --stacktrace --no-configuration-cache --no-daemon
  API_TOKEN: ${{ secrets.API_TOKEN }}
  GOOGLE_SERVICES_JSON: ${{ secrets.GOOGLE_SERVICES_JSON }}
```

`--no-configuration-cache` и `--no-daemon` — потому что раннер одноразовый: кеш конфигурации
негде переиспользовать, а демон только держит память.

JDK **21** во всех джобах, ставится в [`init-gradle`](../.github/actions/init-gradle/action.yaml).
Раньше там стоял 17, а джоба `nav-graph` переопределяла его на 21 ради Layoutlib — разные JDK
в разных джобах обесценивали общий build cache. Байткод при этом по-прежнему собирается
под Java 11 (`Const.JAVA_VERSION`): версия JDK, на котором работает Gradle, и целевая версия
байткода — разные вещи.

## CI (`ci.yml`)

Триггеры — `pull_request` (любая ветка) и `push` в `master` / `releases/**`. Ветки без PR
CI не гоняют намеренно: у `push` и `pull_request` разные `github.ref`, а значит разные группы
`concurrency`, и раньше каждый push в ветку с открытым PR запускал прогон дважды.
`paths-ignore` на `pull_request` **нет**: пропущенный workflow не создаёт чек-ранов, и PR
с правками только в `*.md` навсегда зависал бы в «Expected — Waiting for status to be reported».
Прогон на устаревший коммит отменяется (`concurrency` + `cancel-in-progress`), кроме `master`.
Одиннадцать джоб, запускаются параллельно и между собой не связаны `needs`: падение одной
не останавливает остальные, и в сводке прогона видно сразу все проблемы. Десять из них
блокирующие, `dependency-analysis` — рекомендательная (`continue-on-error`).

| Джоба | Команда / action | Что проверяет | Артефакты и отчёты |
|---|---|---|---|
| `build-app` | `./gradlew assembleDebug` | Проект компилируется. | `debug-apk`; таблица времени сборки в Step Summary. |
| `run-tests` | `./gradlew test` (master) / `runAffectedUnitTests` (ветки и PR) | Юнит-тесты (JVM + Robolectric) и архитектурные тесты `:konsist`. | Аннотации на упавших тестах в diff, `unit-tests-html` при падении. |
| `check-dependency-guard` | `./gradlew :app:dependencyGuard` | Release-classpath не разошёлся со слепком. | — |
| `dependency-analysis` | `./gradlew buildHealth` | Неиспользуемые и неверно объявленные зависимости. Не блокирует (`continue-on-error`). | `dependency-analysis-report`; первые 200 строк отчёта в Step Summary. |
| `run-coverage` | [`actions/coverage`](../.github/actions/coverage/action.yml) | Порог покрытия Kover. | `kover-coverage-html`; таблица LINE/BRANCH в Step Summary. |
| `run-lint` | `./gradlew lint` | Android Lint + кастомные чекеры модуля `:lint`. | `lint-html` (HTML-отчёты всех модулей). |
| `run-detekt` | `./gradlew detekt --continue` | Статический анализ Kotlin, включая Compose-правила (`io.nlopez.compose.rules`). | `detekt.html`, SARIF в Code Scanning, Markdown в Step Summary. |
| `run-ktlint` | [`actions/ktlint`](../.github/actions/ktlint/action.yml) | Форматирование/стиль. | `ktlint-html-report`, SARIF в Code Scanning, Markdown в Step Summary. |
| `check-app-size` | `./gradlew :app:analyzeDebugBundle` | Размер приложения (Ruler). | `ruler-report.html`. |
| `check-module-graph` | `./gradlew :app:assertModuleGraph` + `generateModulesGraphvizText` | Архитектурные границы модулей. | `all_modules.png` (Graphviz), DOT-граф в Step Summary. |
| `nav-graph` | `./gradlew navCheck --continue` + [`actions/nav-graph`](../.github/actions/nav-graph/action.yml) | Граф навигации из аннотаций совпадает с бейзлайнами `*/nav/*.nav`; карта экранов и галерея `@Preview` собираются без ошибок. | `nav-graph-report` (PNG + интерактивный HTML + `index.html`); результат `navCheck` с диффом расхождения и таблицы экранов/переходов в Step Summary. |

### Как устроены отдельные джобы

**`run-coverage`.** Вся логика — в composite action `coverage`: сначала генерируются
отчёты (`:koverHtmlReportFull`, `:koverXmlReportFull`), потом Python-скрипт парсит XML и
печатает таблицу покрытия в `$GITHUB_STEP_SUMMARY`, и только затем запускается гейт
`:koverVerifyFull`. Порядок важен: HTML-отчёт и сводка доступны, даже если порог не пройден.
Само число порога задаётся **только** в корневом `build.gradle.kts`
(`kover { reports { verify { rule { minBound(...) } } } }`) — CI его не дублирует.
Подробности про фильтры и что исключено из знаменателя — в [Testing & Coverage](./testing.md).

**`run-detekt`.** Шаг с Detekt помечен `continue-on-error: true`, чтобы успели выполниться
шаги публикации отчётов; фактический провал джобы делает последний шаг — `grep -q "<error"`
по `detekt.xml`. SARIF уходит в GitHub Code Scanning с `category: detekt`.
Detekt запускается одной агрегированной задачей по всему репозиторию, поэтому плагин
Compose-правил (`detektPlugins(libs.detekt.compose)`) подключён только в корневом
`build.gradle.kts` и проверяет `app`, `core:*` и `feature:*` разом; сами правила — секция
`Compose:` в `config/detekt/detekt.yml`. Версия плагина привязана к detekt: compose-rules
0.5+ требуют detekt 2.x, поэтому на detekt 1.23.x используется линейка 0.4, и Renovate
не поднимает её выше `0.5.0`.

**`run-ktlint`.** `ktlintCheck --continue` собирает SARIF по всем модулям, action склеивает
их в один `merged-ktlint.sarif` через `jq`, отдельно строит подробный Markdown-отчёт
(таблица «модуль → число нарушений» + список с файлами и строками), а
[`actions/report-renderer`](../.github/actions/report-renderer/action.yml) рендерит его в
HTML через Pandoc.

**`check-module-graph`.** Задача `:app:assertModuleGraph` раскрывается в две:
`assertMaxHeight` (высота графа — 6 рёбер, зафиксирована по факту без запаса) и
`assertRestrictions` (запрещённые рёбра регулярками). Правила живут в
[`ModuleGraphConventionPlugin`](../build-logic/convention/src/main/kotlin/ModuleGraphConventionPlugin.kt)
и дополняют, а не дублируют `CheckConventionsPlugin`: самое ценное — `:core:domain` не имеет
права зависеть от data-слоя, чего проверка конвеншенов не ловит вовсе. Оговорка: задача
не считает свою конфигурацию входом, поэтому после правки правил локально нужен
`--rerun-tasks`; в CI каждый прогон и так с чистого листа.
Параллельно архитектуру проверяет конвеншен-плагин `soft.divan.check.conventions`
([`CheckConventionsPlugin.kt`](../build-logic/convention/src/main/kotlin/CheckConventionsPlugin.kt)):
он на `projectsEvaluated` обходит все модули и падает `GradleException`, если
`core` зависит от `feature`, `feature:*:api` — от `impl`, или один `impl` — от чужого `impl`.
Работает это на **любом** запуске Gradle, а не только в этой джобе.

**`nav-graph`.** Гейт `navCheck` (граф навигации из аннотаций совпадает с закоммиченными
`*/nav/*.nav`, см. [nav-graph.md](./nav-graph.md#бейзлайн-nav)) и отчёт с картой экранов
живут в одной джобе. Гейт встроен шагом, а не отдельной джобой: `navCheck` нужен только KSP
модулей с разметкой, а этот KSP джоба и так выполняет ради отчёта. Отдельная джоба была бы
ещё одной холодной сборкой; шаг стоит одну лишнюю фазу конфигурации Gradle.

```mermaid
flowchart LR
    setup["android-setup"] --> check["navCheck --continue<br/>(только KSP)"]
    check -->|"success / failure"| render["actions/nav-graph<br/>рендер Layoutlib"]
    render --> upload["upload nav-graph-report<br/>(if: always)"]
    check -.->|"результат + дифф"| summary["Step Summary"]
    render -.->|"таблицы экранов<br/>и переходов"| summary
```

* `navCheck` идёт **первым и отдельным вызовом Gradle**. Рендер превью через Layoutlib бывает
  flaky; в общем вызове с экспортом его падение остановило бы сборку, и результат `navCheck`
  остался бы неизвестным. `--continue` — чтобы при расхождении увидеть все модули сразу.
  Дифф из блоков «What went wrong» (пути — относительно репозитория) уходит в Step Summary.
* Рендер выполняется с `if: success() || steps.nav-check.outcome == 'failure'`: и при красном
  `navCheck` (по карте видно, что поменялось), но не после упавшего checkout/setup.
  Загрузка `nav-graph-report` — `if: always()`.
* Джобу валит и `navCheck`, и, как раньше, падение рендера, но шаги разделены: зелёный
  «Check nav graph baselines» значит, что граф в порядке, даже если джоба красная.

Расхождение лечится `./gradlew navDump` и коммитом `.nav` — если изменение навигации
осознанное. Иначе это сигнал, что экран потерял или получил переход по ошибке.

**`run-tests`.** На `master` гоняется полный `./gradlew test`. На ветках и в PR —
`runAffectedUnitTests` из плагина
[AffectedModuleDetector](https://github.com/dropbox/AffectedModuleDetector): он сравнивает
диффс merge-base `master`, строит список изменённых модулей и их обратных зависимостей и
запускает тесты только для них. Поэтому `checkout` здесь с `fetch-depth: 0` — без полной
истории merge-base не найти. Правка `gradle/libs.versions.toml`, `build-logic`, корневых
`*.gradle.kts` или `gradle.properties` считается задевающей всё (`pathsAffectingAllModules`)
и возвращает полный прогон. Модуль `:konsist` из детектора исключён и запускается явно: он
читает исходники всех модулей с диска, и граф зависимостей Gradle этой связи не видит.
Результаты публикует `mikepenz/action-junit-report` — упавший тест виден аннотацией прямо
на строке, а не только в логе.

**`check-dependency-guard` и `dependency-analysis`.** Две проверки, намеренно разнесённые
по разным джобам: сначала они были склеены, и падение рекомендательного отчёта утаскивало
за собой блокирующий гейт, который даже не успевал запуститься.

* `buildHealth` ([dependency-analysis](https://github.com/autonomousapps/dependency-analysis-gradle-plugin))
  — **рекомендательный**: ищет неиспользуемые зависимости, `api` вместо `implementation`
  и использование транзитивных зависимостей без явного объявления. Все категории настроены
  на `severity("warn")` в корневом `build.gradle.kts`: на текущей кодовой базе отчёт занимает
  ~950 строк, и разбирать его нужно постепенно. По мере разбора категории переводятся
  на `fail` поштучно. Джоба помечена `continue-on-error` и не входит в required-чеки:
  на холодном кэше это ~6700 задач и заметно дольше остальных джоб, поэтому merge её
  не ждёт. Там же свои настройки памяти. Задачи плагина исполняются через
  `IsolatedClassloaderWorker` — каждый work item получает собственный classloader, и его
  классы оседают в Metaspace. Дефолтного `-XX:MaxMetaspaceSize=1g` из `gradle.properties`
  на такой объём не хватает: джоба падала с `OutOfMemoryError: Metaspace`. Поэтому здесь
  `-XX:MaxMetaspaceSize=2g` и `--max-workers=2` вместо `--parallel`.
  Локально это воспроизводится только с `--rerun-tasks`: на тёплом кэше почти все задачи
  становятся `UP-TO-DATE`, и холодный путь не проверяется вовсе.
* `:app:dependencyGuard` ([dependency-guard](https://github.com/dropbox/dependency-guard))
  — **блокирующий**: сверяет release-runtime-classpath приложения со слепком
  [`app/dependencies/releaseRuntimeClasspath.txt`](../app/dependencies/releaseRuntimeClasspath.txt).
  Любое изменение дерева зависимостей, включая транзитивное, приезжает в PR явным diff'ом.
  Обновить слепок осознанно: `./gradlew :app:dependencyGuardBaseline`.

## Security (`security.yml`)

Две джобы, триггеры те же, что у `ci.yml` (`pull_request` + `push` в `master` / `releases/**`):

* **`secret-scan`** — [gitleaks](https://github.com/gitleaks/gitleaks-action) по всей истории
  (`fetch-depth: 0`, иначе секрет, удалённый последним коммитом, не найдётся). Прямо
  поддерживает требование [`docs/agents/security.md`](./agents/security.md): `API_TOKEN`,
  `YANDEX_CLIENT_ID` и JWT живут только в `local.properties` и CI-секретах, а пароли от
  keystore — только в переменных окружения (в CI — из секретов `JKS_*`). Лицензия нужна только организациям — для личного аккаунта бесплатно.
  Отдельный шаг разбирает SARIF через `jq` и пишет результат в Step Summary: на зелёном
  прогоне — сколько правил отработало, на красном — таблица «правило → файл → строка».
  Значение найденного секрета (`region.snippet`) в сводку намеренно не попадает:
  напечатанный в лог публичного репозитория, он утёк бы второй раз.
  В Code Scanning SARIF **не** загружается — этого не рекомендуют сами авторы gitleaks:
  алерт там можно закрыть как resolved, тогда как секрет остаётся в истории git и
  остаётся скомпрометированным. Лечение всегда одно — ротация самого секрета.
* **`dependency-review`** (только на PR) — блокирует добавление зависимости с известной
  уязвимостью уровня `high` и выше. Работает поверх графа, который публикует
  `dependency-submission.yml`, поэтому без него бесполезен.

## Доска задач (`board.yml`, `ci-failure.yml`)

Эти два workflow не проверяют код, а ведут доску задач; модель доски описана в
[Доске задач](./task-tracking.md), здесь — только то, что нужно знать про CI.

* **`board.yml`** — три независимые джобы: `branch-started` (push ветки `feature/FM-*`),
  `pr-sync` (события `pull_request`, только PR из этого же репозитория) и `digest`
  (понедельник 06:00 UTC и ручной запуск; по умолчанию ручной запуск только печатает
  сводку). Вся логика — в [`.github/scripts/board.py`](../.github/scripts/board.py).
* **`ci-failure.yml`** — реагирует на завершение `CI` и `Security` на `master`. Логика — в
  [`ci_failure.py`](../.github/scripts/ci_failure.py). Событие `workflow_run` работает только с
  файла из ветки по умолчанию, поэтому workflow включается после мержа в `master`.
* Доску меняет секрет `PROJECT_TOKEN` (classic PAT, scope `project`): токен Actions не может
  менять доски пользователя. Остальные операции идут с токеном Actions и минимальными правами
  (`pull-requests: write`, `issues: write`).
* Скрипты — на stdlib Python, их чистая логика покрыта тестами; в CI они не подключены:
  `python3 -m unittest discover -s .github/scripts -p 'test_*.py'`.

## CD: тестовые сборки (`cd_tests.yml`)

Триггер — `push` в `tests/**` либо ручной `workflow_dispatch`.

```mermaid
flowchart LR
    b["build-app<br/>:app:assembleDebug"] --> t["report-telegram<br/>APK в чат (тред «test»)"]
    b --> f["distribute-app-firebase<br/>Firebase App Distribution"]
```

`build-app` собирает debug-APK и заливает его артефактом `app-debug`; обе следующие джобы
скачивают этот артефакт (пересборки нет). Телеграм-джоба помечена `continue-on-error: true` —
недоступность бота не должна валить доставку. Раздача в Firebase идёт на группы из секрета
`FIREBASE_GROUPS`, в release notes попадают версия, ветка, сообщение коммита и ссылка на прогон.

## CD: релиз (`cd_release.yml`)

Триггер — `push` в `releases/**` либо `workflow_dispatch`.

```mermaid
flowchart LR
    v["validate-version<br/>парсит имя ветки"] --> apk["build-apk<br/>assembleRelease"]
    v --> aab["build-aab<br/>bundleRelease"]
    apk --> tg["report-telegram<br/>APK в чат (тред «release»)"]
    aab --> play["publish-play<br/>Google Play, track=internal, status=draft"]
    apk --> gh["github-release<br/>черновик релиза: тег v.X.Y.Z,<br/>APK + AAB, авто-changelog"]
    aab --> gh
```

Джоба `github-release` создаёт именно **черновик**: тег и release notes GitHub собирает сам
(`generate_release_notes: true` — из PR и коммитов с прошлого тега), а публикует релиз
человек кнопкой Publish. Симметрично `status: draft` в Play — ни одна из двух публикаций
не происходит автоматически.

**Версия берётся из имени ветки.** Джоба `validate-version` требует, чтобы ветка
заканчивалась на `v.X.Y.Z` (например `releases/v.1.2.3`), иначе прогон падает. Из неё
вычисляются:

```
VERSION_NAME = X.Y.Z
VERSION_CODE = X * 1_000_000 + Y * 1_000 + Z
```

и передаются в Gradle как `-PversionCode` / `-PversionName`. Конвеншен-плагин
[`AndroidAppConventionPlugin`](../build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt)
читает эти проектные свойства и подставляет их в `defaultConfig`, откатываясь на
константы из [`Const.kt`](../build-logic/convention/src/main/kotlin/Const.kt) (`0.0.1` / `1`),
если свойств нет — то есть локальная сборка всегда собирается как `0.0.1`.

**Подпись.** Keystore хранится в секрете `JKS_KS` в base64, шаг `Decode Keystore`
раскладывает его в `app/release.jks` (файл в `.gitignore`). Сам `signingConfig` объявлен в
[`ConfigureBaseAndroid.kt`](../build-logic/convention/src/main/kotlin/soft/divan/financemanager/ConfigureBaseAndroid.kt)
и читает пароли **только** из переменных окружения `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD` — в репозитории их нет. Для `:app` в release дополнительно включены
`isMinifyEnabled` и `isShrinkResources` (R8 + шринк ресурсов).

**Публикация.** `r0adkll/upload-google-play@v1` кладёт AAB на трек `internal` со статусом
`draft` — то есть релиз создаётся, но не раскатывается; финальную публикацию делает человек
в Play Console.

## Composite actions

Переиспользуемые кирпичи в `.github/actions/`:

| Action | Что делает |
|---|---|
| [`android-setup`](../.github/actions/android-setup/action.yml) | Зонтичный: checkout → `init-gradle` → `create-google-services`. Одна строка в джобе вместо трёх. |
| [`init-gradle`](../.github/actions/init-gradle/action.yaml) | JDK 21 (Temurin) + `gradle/actions/setup-gradle` + `chmod +x gradlew`. |
| [`create-google-services`](../.github/actions/create-google-services/action.yml) | Декодирует секрет `GOOGLE_SERVICES_JSON` (base64) в `app/google-services.json`; падает, если секрет пуст. |
| [`coverage`](../.github/actions/coverage/action.yml) | Kover: отчёты → сводка в Step Summary → гейт `koverVerifyFull`. |
| [`ktlint`](../.github/actions/ktlint/action.yml) | `ktlintCheck`, склейка SARIF по модулям, подробный Markdown-отчёт. |
| [`report-renderer`](../.github/actions/report-renderer/action.yml) | Markdown → styled HTML через Pandoc (со светлой и тёмной темой). |
| [`build-time-report`](../.github/actions/build-time-report/action.yaml) | CSV от `build-time-tracker` → Markdown-таблица в Step Summary (`csv2md.sh`). |
| [`draw-graph`](../.github/actions/draw-graph/action.yaml) | DOT → PNG через Graphviz. |
| [`nav-graph`](../.github/actions/nav-graph/action.yml) | Рендер карты навигации и галереи `@Preview` (PNG + HTML), лендинг `index.html`, таблицы экранов и переходов в Step Summary. Гейт `navCheck` — не здесь, а отдельным шагом джобы. |
| [`send-file-tg`](../.github/actions/send-file-tg/action.yaml) | Отправка файла и подписи в Telegram (`sendDocument`, поддержка тредов). |
| [`send-message-tg`](../.github/actions/send-message-tg/action.yml) | Отправка текста в Telegram (`sendMessage`, поддержка тредов). Значения передаются через `env:`, текст уходит без разметки. |

## Секреты

Все — на уровне репозитория (GitHub Actions secrets); окружений (Environments) с
protection rules сейчас нет.

| Секрет | Где используется | Зачем |
|---|---|---|
| `API_TOKEN` | все workflow (env) | Токен бэкенда, читается сборкой `:core:network` (локально — из `local.properties`). |
| `GOOGLE_SERVICES_JSON` | все workflow (env) | base64 `google-services.json` для Firebase/Crashlytics. |
| `JKS_KS` | `cd_release` | base64 release-keystore. |
| `JKS_KS_PASS`, `JKS_ALIAS`, `JKS_KEY_PASS` | `cd_release` | Пароль стора, алиас и пароль ключа. |
| `PLAY_SERVICE_ACCOUNT_JSON`, `PLAY_PACKAGE_NAME` | `cd_release` | Сервисный аккаунт и applicationId для Play Publishing API. |
| `FIREBASE_APP_ID_DEBUG`, `FIREBASE_SERVICE_ACCOUNT`, `FIREBASE_GROUPS` | `cd_tests` | App Distribution. |
| `TG_TOKEN`, `TG_CHAT_BUILD`, `TG_THREAD_TEST`, `TG_THREAD_RELEASE` | `cd_tests`, `cd_release` | Бот, чат и треды для отчётов о сборках. |
| `PROJECT_TOKEN` | `board`, `ci-failure` | Classic PAT со scope `project`: менять карточки доски задач. Истекает — срок стоит отслеживать. |
| `TG_THREAD_BOARD` | `board` | Тема чата для недельной сводки; необязателен, без него сводка уходит в общий чат. |

> `YANDEX_CLIENT_ID` (см. [`app/build.gradle.kts`](../app/build.gradle.kts)) читается по той же
> схеме «env или `local.properties`», но как CI-секрет пока **не** заведён — в CI-сборках
> плейсхолдер пустой.

## Что на стороне Gradle

CI намеренно тонкий, поэтому «где что настроено» полезно держать в голове:

| Инструмент | Где настроен | Задача |
|---|---|---|
| Kover (покрытие + гейт) | корневой `build.gradle.kts` | `koverHtmlReportFull`, `koverXmlReportFull`, `koverVerifyFull` |
| Detekt (+ Compose-правила `io.nlopez.compose.rules`) | корневой `build.gradle.kts` + `config/detekt/detekt.yml` | `detekt` (xml + html + sarif + md) |
| KtLint | `subprojects { … }` в корневом `build.gradle.kts` | `ktlintCheck` / `ktlintFormat` |
| Android Lint + кастомные правила | конвеншен-плагины, модуль `:lint` | `lint` |
| Ruler (размер приложения) | [`RulerConventionPlugin`](../build-logic/convention/src/main/kotlin/RulerConventionPlugin.kt) | `:app:analyzeDebugBundle` |
| Время сборки | [`BuildTimeTrackerConventionPlugin`](../build-logic/convention/src/main/kotlin/BuildTimeTrackerConventionPlugin.kt) | CSV в `app/build/reports/buildTimeTracker/` |
| Архитектурные границы | [`CheckConventionsPlugin`](../build-logic/convention/src/main/kotlin/CheckConventionsPlugin.kt) | выполняется на любом запуске Gradle |
| Граф модулей | [`ModuleGraphConventionPlugin`](../build-logic/convention/src/main/kotlin/ModuleGraphConventionPlugin.kt) | `:app:assertModuleGraph` (высота + запрещённые рёбра), `:app:generateModulesGraphvizText` |
| Архитектура на уровне классов | модуль [`:konsist`](../konsist/README.md) | `:konsist:test` (входит в обычный `test`) |
| Анализ зависимостей | корневой `build.gradle.kts` + `AndroidBaseConventionPlugin` / `JvmLibraryConventionPlugin` | `buildHealth` |
| Бейзлайн графа навигации | [`NavGraph.kt`](../build-logic/convention/src/main/kotlin/soft/divan/financemanager/NavGraph.kt) (`configureNavGraph()`), плагин `com.github.skydoves.navgraph` | `navCheck` / `navDump`, экспорт карты и галереи превью |
| Слепок release-classpath | [`DependencyGuardConventionPlugin`](../build-logic/convention/src/main/kotlin/DependencyGuardConventionPlugin.kt) | `:app:dependencyGuard`, `:app:dependencyGuardBaseline` |
| Отбор затронутых модулей | корневой `build.gradle.kts` (AffectedModuleDetector) | `runAffectedUnitTests` |
| Диагностика скорости сборки | корневой `build.gradle.kts` (Gradle Doctor) | выполняется на любом запуске Gradle |
| Build scan | `settings.gradle.kts` (Develocity) | `--scan -Pdevelocity.tos.agree=true` |
| Версия и подпись | `AndroidAppConventionPlugin`, `ConfigureBaseAndroid`, `Const.kt` | `-PversionName` / `-PversionCode`, `signingConfigs` |

**Gradle Doctor** ([`com.osacky.doctor`](https://github.com/runningcode/gradle-doctor)) печатает
«рецепты» после каждой сборки: промахи build cache, время в GC, зависимости от `clean`,
незаданный `JAVA_HOME` (из-за него переключение между Android Studio и терминалом вызывает
полную пересборку). Настроен предупреждать, а не ронять сборку: `javaHome.failOnError = false`
и выключен `disallowMultipleDaemons` — CI и так гоняет Gradle с `--no-daemon`.

**Build scan** публикуется на публичный `scans.gradle.com`, поэтому по умолчанию выключен:
это требует согласия с условиями использования Gradle. Включается явно одним запуском —
`./gradlew assembleDebug --scan -Pdevelocity.tos.agree=true`.

## Локальный прогон «как в CI»

```bash
./gradlew assembleDebug test koverVerifyFull lint detekt ktlintCheck :app:assertModuleGraph :app:dependencyGuard navCheck
./gradlew buildHealth
```

`buildHealth` вынесен во второй запуск намеренно: dependency-analysis добавляет по десятку
своих задач на каждый модуль, и вместе с полной сборкой это укладывает Gradle-демон по
памяти (`Gradle build daemon disappeared unexpectedly`). В CI они и так живут в разных
джобах, так что проблема только локальная.

Быстрее — проверять только затронутые модули (`./gradlew :feature:<name>:impl:check`),
а полный набор гонять перед пушем.

---

## Ограничения

- **Одиннадцать джоб — одиннадцать холодных сборок.** Общего build cache между джобами нет,
  поэтому новые проверки по возможности встраиваются шагом в джобу, которая уже собирает
  нужное, — так подключён `navCheck`.
- **CI и CD пересекаются на `releases/**`.** Релизная ветка проходит полный гейт качества
  параллельно со сборкой, но публикация его не ждёт.
- **Скриншот-тесты Compose Preview не подключаются** — подробности в
  [testing.md → Ограничения](./testing.md#ограничения).

## Связанные задачи

Открытые задачи по CI/CD — на доске, [фильтр `area:ci`](https://github.com/IliaVoskoboinikov/Finance-Manager/issues?q=is%3Aissue%20is%3Aopen%20label%3Aarea%3Aci);
обоснование и способ исправления — в каждой карточке. Что важно знать о текущем состоянии
пайплайна:

| Задача | Что сейчас не так |
| :--- | :--- |
| [FM-70](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/70) | `send-file-tg` подставляет сообщение коммита прямо в `run:` — инъекция шелла на раннере с доступом к keystore |
| [FM-61](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/61) | `cd_release.yml` публикует в Play, не дожидаясь тестов и линтеров |
| [FM-72](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/72), [FM-73](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/73) | release-сборка (R8) в CI не собирается и сейчас падает |
| [FM-71](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/71) | в `cd_tests.yml` и `cd_release.yml` нет явного блока `permissions:` |
| [FM-74](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/74) | `YANDEX_CLIENT_ID` не передаётся в сборки CI и CD — вход через Яндекс в них не работает |
| [FM-75](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/75) | `printVersionName` не знает про `-PversionName`, Telegram-отчёты показывают неверную версию |
| [FM-77](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/77) | тесты в CI идут дважды: в `run-tests` и в `run-coverage` |

## Ключевые файлы

| Файл | Роль |
|---|---|
| [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) | Гейт качества на каждый push. |
| [`.github/workflows/cd_tests.yml`](../.github/workflows/cd_tests.yml) | Тестовая раздача (Telegram + Firebase). |
| [`.github/workflows/cd_release.yml`](../.github/workflows/cd_release.yml) | Подписанный релиз и публикация в Play. |
| [`.github/workflows/security.yml`](../.github/workflows/security.yml) | gitleaks + dependency-review. |
| [`.github/workflows/dependency-submission.yml`](../.github/workflows/dependency-submission.yml) | Граф зависимостей для Dependabot alerts. |
| [`.github/workflows/board.yml`](../.github/workflows/board.yml) | Статусы карточек по веткам и PR, недельная сводка. |
| [`.github/workflows/ci-failure.yml`](../.github/workflows/ci-failure.yml) | Красный `master` → issue. |
| [`.github/scripts/`](../.github/scripts/) | `board.py`, `ci_failure.py` и их тесты. |
| [`.github/renovate.json5`](../.github/renovate.json5) | Правила автообновления зависимостей и actions. |
| [`.coderabbit.yaml`](../.coderabbit.yaml) | Настройки AI-ревьюера CodeRabbit и правила по областям кода. |
| [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) | Общие правила AI-ревью PR. |
| [`.github/actions/`](../.github/actions/) | Composite actions: setup, отчёты, доставка. |
| [`konsist/`](../konsist/README.md) | Архитектурные тесты уровня классов. |
| [`app/dependencies/`](../app/dependencies/) | Слепок release-classpath (dependency-guard). |
| [`build.gradle.kts`](../build.gradle.kts) | Kover (фильтры + порог), Detekt, KtLint. |
| [`build-logic/convention/`](../build-logic/convention/) | Версия, подпись, R8, Ruler, build-time tracker, проверка архитектуры. |
| [`config/detekt/detekt.yml`](../config/detekt/detekt.yml) | Правила Detekt. |
