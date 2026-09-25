import com.aalekh.aalekh.model.ModuleType
import com.aalekh.aalekh.model.Severity
import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.graph) apply false
    alias(libs.plugins.build.time.tracker) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.lint)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.navgraph) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.gradle.doctor)
    alias(libs.plugins.affected.module.detector)
}

/**
 * Определение затронутых модулей по git-диффу (`./gradlew runAffectedUnitTests`).
 *
 * На 30+ модулях полный прогон тестов ради правки одной фичи — основная статья
 * расхода времени CI. Плагин строит список изменённых модулей и их обратных
 * зависимостей и запускает тесты только для них.
 *
 * На master в CI по-прежнему гоняется полный `test`: быстрый путь — для веток и PR,
 * полная проверка — для основной ветки.
 */
affectedModuleDetector {
    baseDir = "${project.rootDir}"
    // Правка любого из этих путей задевает вообще всё — тогда прогон полный.
    pathsAffectingAllModules = setOf(
        "gradle/libs.versions.toml",
        "gradle/wrapper/gradle-wrapper.properties",
        "build-logic",
        "build.gradle.kts",
        "settings.gradle.kts",
        "gradle.properties"
    )
    // :konsist проверяет исходники всех модулей, читая их с диска, — граф зависимостей
    // Gradle этой связи не видит, поэтому модуль исключён из «умного» отбора и
    // запускается всегда через обычный test.
    excludedModules = setOf(":konsist")
    // Порядок важен: сеттер compareFrom валидирует, что ветка уже задана.
    // Именно origin/master, а не master: actions/checkout приносит ветку только как
    // remote-ref, локального refs/heads/master в раннере нет и merge-base не найдётся.
    specifiedBranch = "origin/master"
    compareFrom = "SpecifiedBranchCommitMergeBase"
    buildAllWhenNoProjectsChanged = true
}

/**
 * Gradle Doctor — диагностика того, что замедляет сборку: промахи build cache,
 * время в GC, «отрицательная экономия» от incremental-задач, зависимости от `clean`.
 *
 * Настроен так, чтобы предупреждать, а не ронять сборку на особенностях
 * окружения разработчика.
 */
doctor {
    javaHome {
        // JAVA_HOME системы почти всегда отличается от JBR, на котором запускается
        // Gradle из Android Studio. Сообщаем, но не падаем.
        failOnError = false
    }
    // CI запускает Gradle с --no-daemon, локально демон один — проверка избыточна.
    disallowMultipleDaemons = false
}

/**
 * Анализ зависимостей всех модулей (`./gradlew buildHealth`).
 *
 * Ловит то, чего не видят ни `assertModuleGraph` (он про рёбра между модулями),
 * ни detekt: неиспользуемые зависимости, `api` вместо `implementation`,
 * транзитивные зависимости, которыми модуль пользуется, не объявляя их.
 */
dependencyAnalysis {
    issues {
        all {
            // Пока весь анализ носит рекомендательный характер: на текущей кодовой базе
            // отчёт занимает ~950 строк, и большая часть — «объяви транзитивную
            // зависимость явно». Разбирать это нужно постепенно, а не одним коммитом,
            // поэтому CI-джоба buildHealth не блокирует сборку, а публикует отчёт.
            // По мере разбора категории переводятся на severity("fail") поштучно.
            onAny { severity("warn") }

            // Юнит-тестовый стек (junit/mockk/assertj) добавляется convention-плагинами
            // всем модулям сразу — в модулях без тестов он «неиспользуемый» by design.
            onUnusedDependencies { severity("warn") }

            // Аннотационные процессоры (Hilt/Room/KSP) подключаются осознанно,
            // анализатор их использование по исходникам не видит.
            onUnusedAnnotationProcessors { severity("ignore") }
        }
    }
}

/**
 * Агрегированное покрытие юнит-тестами (задачи koverHtmlReportFull / koverVerifyFull).
 *
 * Из метрики исключены сгенерированный код (Hilt/Dagger, Room `*_Impl`, BuildConfig,
 * Compose singletons) и UI-слой (@Composable/@Preview, Activity/App, navigation
 * `*FeatureImpl`) — Compose-тесты осознанно отложены; всё остальное держим на уровне 98%.
 */
kover {
    merge {
        allProjects()
        createVariant("full") {
            add("jvm", optional = true)
            add("debug", optional = true)
        }
    }
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                annotatedBy(
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.ui.tooling.preview.Preview",
                    "dagger.internal.DaggerGenerated",
                    "javax.annotation.processing.Generated",
                )
                classes(
                    // Сгенерированный Dagger/Hilt-код
                    "*Factory",
                    "*Factory\$*",
                    "*_MembersInjector",
                    "Dagger*",
                    "*_HiltModules*",
                    "Hilt_*",
                    "*.Hilt_*",
                    // Room / BuildConfig
                    "*_Impl",
                    "*_Impl\$*",
                    "*.BuildConfig",
                    // Синтетика Kotlin для интерфейсов с дефолтными реализациями
                    "*\$DefaultImpls",
                    // Compose / Android UI (тесты отложены, см. docs/testing.md).
                    // *ScreenKt — файлы-фасады @Composable-экранов: annotatedBy(@Composable)
                    // исключает методы, но не сам facade-класс, поэтому добавлены по имени.
                    "*ComposableSingletons*",
                    "*ScreenKt",
                    "*ScreenKt\$*",
                    "*Activity",
                    "*Activity\$*",
                    "soft.divan.financemanager.App",
                    "*FeatureImpl",
                    "*FeatureImpl\$*",
                )
                packages(
                    "hilt_aggregated_deps",
                    "dagger.hilt.internal.aggregatedroot.codegen",
                    "*.databinding",
                )
            }
        }
        verify {
            rule("Line coverage of testable code is at least 95%") {
                minBound(95)
            }
        }
    }
}

buildscript {
    dependencies {
        classpath(libs.ruler.plugin)
        // Как и ruler: convention-плагин применяет его по id, значит плагин должен
        // лежать на runtime-classpath сборки, а не только compileOnly в build-logic.
        classpath(libs.dependency.guard.plugin)
    }
}

subprojects {
    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set("1.8.0")
            debug.set(true)
            verbose.set(true)
            android.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(false)

            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.HTML)
                reporter(ReporterType.SARIF)
            }
        }
    }
}

/**
 * Aalekh — анализ и enforcement архитектуры графа модулей (`./gradlew aalekhReport` /
 * `aalekhCheck`). Плагин применяется в [settings.gradle.kts] (settings-вариант), а весь
 * контракт архитектуры описан здесь, в единственном блоке `aalekh { }`.
 *
 * Что он закрывает сверх уже имеющихся проверок:
 * - `CheckConventionsPlugin` (build-logic) проверяет применение конвеншен-плагинов и грубые
 *   рёбра, но не даёт ни отчёта, ни диффа в PR;
 * - `:app:assertModuleGraph` (jraska) на данный момент правил не содержит и по факту `UP-TO-DATE`
 *   (см. help_comand.md) — Aalekh закрывает именно это: машинно-проверяемые слои, изоляция фич,
 *   reachability, метрики и интерактивный HTML-отчёт.
 *
 * Слои повторяют Clean Architecture проекта: foundation (инфраструктурные core) → domain
 * (чистый Kotlin) → data (Room/Retrofit/sync) → ui (дизайн-система) → feature (:feature:*:impl/api)
 * → app. `canOnlyDependOn` перечисляет слои, «вниз» по которым модулю разрешено зависеть; рёбра
 * внутри одного слоя разрешены всегда. Модуль попадает в ПЕРВЫЙ подходящий по шаблону слой.
 *
 * CI-джоба `check-architecture` в .github/workflows/ci.yml запускает `aalekhCheck` как
 * блокирующую проверку; текущие (унаследованные) нарушения заморожены в `aalekh-baseline.json`,
 * поэтому падение возможно только на НОВЫХ. Полное описание — docs/aalekh.md.
 */
aalekh {
    // В CI браузер открывать нельзя; локально путь к отчёту печатается в лог — открывается вручную.
    openBrowserAfterReport.set(false)
    // Тестовые рёбра оставляем — важны для полноты графа и метрик связности.
    includeTestDependencies.set(true)
    // compileOnly архитектурно не значим — по умолчанию за бортом, фиксируем явно.
    includeCompileOnlyDependencies.set(false)
    // Внешние координаты (group:name:version) читаются как объявлены, без резолва, — показываются
    // в инспекторе модулей и на вкладке Dependencies (ловит расхождения версий библиотек).
    includeExternalDependencies.set(true)
    // Писать aalekh-metrics.csv рядом с HTML на каждый aalekhReport — для внешних дашбордов/трендов.
    exportMetrics.set(true)

    // Слепок нарушений (freeze). Падаем только на новых. Обновлять: ./gradlew aalekhBaseline.
    baselineFile.set("aalekh-baseline.json")
    // Слепок архитектуры для aalekhDiff (комментарий в PR). Обновлять: ./gradlew aalekhSnapshot.
    snapshotFile.set("aalekh-snapshot.json")
    // aalekhDiff по умолчанию только отчитывается; ронять его на новом цикле/регрессии метрики
    // не нужно — за это отвечает блокирующий aalekhCheck (см. qualityGates ниже).
    failOnArchitectureRegression.set(false)

    // Временна́я (change) связанность из git-истории: aalekhTemporal → aalekh-temporal.md/.json.
    temporalCoupling {
        commitWindow.set(500)
        minSharedCommits.set(2)
        hiddenCouplingThreshold.set(0.6)
    }

    // Диапазон для aalekhAffected (blast radius по диффу). В CI baseRef переопределяется на
    // origin/master, локально по умолчанию сравнение с рабочим деревом от HEAD~1.
    affected {
        baseRef.set("origin/master")
        headRef.set("")
    }

    // Экспорт диаграмм (aalekhMermaid): чисто инструментальные модули из картинки убираем.
    mermaid {
        exclude(":lint")
        exclude(":konsist")
    }

    // ── Слои: foundation → domain → data → ui → feature → app ────────────────────────────────
    layers {
        // Инфраструктурные core-модули. Без canOnlyDependOn — не ограничиваем, чтобы не ловить
        // ложные срабатывания (например :core:auth, использующий :core:network).
        layer("foundation") {
            modules(
                // :core — пустой контейнер-namespace, который Gradle материализует для вложенных
                // путей; кладём сюда, чтобы покрытие слоями было исчерпывающим (иначе uncovered).
                ":core",
                ":core:common",
                ":core:security",
                ":core:auth",
                ":core:logging-error",
                ":core:feature-api",
                ":core:workmanager",
                ":core:notifications",
            )
        }
        // Чистый Kotlin: сущности, UseCase, интерфейсы репозиториев. Ничего, кроме foundation.
        layer("domain") {
            modules(":core:domain")
            canOnlyDependOn("foundation")
        }
        // Реализация репозиториев, Room, Retrofit и фоновая синхронизация.
        layer("data") {
            modules(
                ":core:data",
                ":core:database",
                ":core:network",
                ":sync",
            )
            canOnlyDependOn("foundation", "domain")
        }
        // Дизайн-система (Compose + Material 3).
        layer("ui") {
            modules(":core:uikit")
            canOnlyDependOn("foundation", "domain", "data")
        }
        // Презентационный слой: контракты (:api) и реализации (:impl) фич.
        layer("feature") {
            modules(":feature:**")
            canOnlyDependOn("foundation", "domain", "data", "ui")
        }
        // Хост: собирает граф и владеет корневой навигацией.
        layer("app") {
            modules(":app")
            canOnlyDependOn("foundation", "domain", "data", "ui", "feature")
        }
        // Инструментальные модули без продуктового кода — отдельный слой, чтобы покрытие слоями
        // (requireLayerForAllModules) было исчерпывающим и они не висели в Unclassified.
        layer("quality") {
            modules(":lint", ":konsist")
        }
    }

    // Изоляция фич: :feature:*:impl не должны зависеть друг от друга — только через чужие :api
    // (:api под шаблон не подпадает, поэтому impl → чужой api разрешён).
    featureIsolation {
        featurePattern = ":feature:*:impl"
    }

    // Владение модулями — оверлей на графе и в инспекторе; помечает межкомандные рёбра.
    teams {
        team("core-platform") { modules(":core:**") }
        team("features") { modules(":feature:**") }
        team("app") { modules(":app", ":sync") }
        team("quality") { modules(":lint", ":konsist") }
    }

    rules {
        // Каждый модуль обязан попасть в объявленный слой (иначе layer-dependency его не видит).
        requireLayerForAllModules()

        // Бюджеты графа (WARNING): высота = минимум последовательных шагов компиляции.
        // Лимит транзитивных зависимостей чуть выше текущего пика (:app = 45) — ловит рост,
        // не шумя на нормальном для хост-модуля агрегате.
        maxGraphHeight(12)
        noTransitiveDependenciesExceeding(50)

        // Модули, которые никто не использует и которые ни на что не ссылаются (WARNING).
        // Исключаем: (1) инструментальные модули — они подключаются особыми конфигурациями
        // (lintChecks и т.п.), графом не видными; (2) пустые контейнеры-namespace (:core,
        // :feature, :feature:<name>), которые Gradle материализует для вложенных путей.
        noOrphanModules()
        rule("no-orphan-modules") {
            suppressFor(":lint")
            suppressFor(":konsist")
            suppressFor(":core")
            suppressFor(":feature")
            suppressFor(":feature:*")
        }

        // Запрет циклов включён всегда; дополнительно фиксируем регрессию количества циклов —
        // новый цикл роняет сборку, даже если какие-то уже существовали.
        rule("no-cyclic-dependencies") {
            preventRegression = true
        }

        // Reachability (транзитивная достижимость), ERROR:
        // домен не должен дотягиваться до приложения даже через цепочку.
        forbidReachable(
            from = ":core:domain",
            to = ":app",
            because = "домен остаётся независимым от приложения даже транзитивно",
        )
        // Общий инфраслой не должен дотягиваться до фич (зависимость обязана быть однонаправленной).
        forbidReachable(
            from = ":core:common",
            to = ":feature:**",
            because = "общий core остаётся feature-agnostic",
        )
        // Дизайн-система не должна дотягиваться до слоя данных.
        forbidReachable(
            from = ":core:uikit",
            to = ":core:data",
            because = "дизайн-система не должна знать о слое данных",
        )
        // Каждая реализация фичи обязана быть достижима из :app — иначе это мёртвый код.
        mustBeReachableFrom(
            module = ":feature:*:impl",
            from = ":app",
            because = "нереференсная фича собирается, но никому не поставляется",
        )
    }

    // Инлайн-предикаты «X не должен зависеть от Y».
    // Фичи — листья графа: зависеть от хоста-приложения нельзя.
    forbid {
        from(":feature:**")
        to(":app")
        because("фичи — независимые срезы; зависимость от app инвертирует граф")
    }
    // Домен обязан оставаться платформо-независимым (pure Kotlin), чтобы его можно было делить в KMP.
    forbid {
        from(":core:domain")
        toModuleType(ModuleType.ANDROID_LIBRARY)
        because("доменный слой не должен зависеть от Android-модулей")
    }

    // Ratchet структурных метрик: любая из них не должна ухудшиться относительно baseline.
    // Срабатывает только после ./gradlew aalekhBaseline (он пишет снимок метрик в baseline).
    qualityGates {
        forbidAllRegressions()
        severity.set(Severity.ERROR)
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    group = "verification"
    description = "Run Detekt on all modules (aggregated)"

    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = false

    config.setFrom(file(File(rootDir, "config/detekt/detekt.yml")))
    setSource(files(rootDir))

    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
    exclude("**/.gradle/**")
    exclude("**/generated/**")

    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
