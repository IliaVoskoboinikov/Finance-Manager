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
