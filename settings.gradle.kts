pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("com.gradle.develocity") version "4.5.0"
}

/**
 * Build scan — детальный разбор сборки (что и почему пересобиралось, куда ушло время,
 * промахи build cache). Полезен при разборе «почему CI собирается 8 минут».
 *
 * Публикация выключена по умолчанию: скан уходит на публичный scans.gradle.com,
 * а это требует согласия с условиями Gradle. Включается явно:
 *
 * ```bash
 * ./gradlew assembleDebug --scan -Pdevelocity.tos.agree=true
 * ```
 */
develocity {
    val termsAccepted = providers.gradleProperty("develocity.tos.agree").orNull == "true"
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = if (termsAccepted) "yes" else "no"
        publishing.onlyIf { termsAccepted }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "FinanceManager"
include(":app")
// Тестовый модуль: архитектурные правила уровня классов (Konsist), без продуктового кода.
include(":konsist")
include(":core:network")
include(":core:uikit")
include(":core:domain")
include(":core:data")
include(":core:auth")
include(":core:security")
include(":feature:settings:api")
include(":feature:settings:impl")
include(":core:feature-api")
include(":feature:transactions-today:api")
include(":feature:transactions-today:impl")
include(":feature:category:api")
include(":feature:category:impl")
include(":feature:my-accounts:api")
include(":feature:my-accounts:impl")
include(":feature:transaction:api")
include(":feature:transaction:impl")
include(":core:database")
include(":feature:splash-screen:api")
include(":feature:splash-screen:impl")
include(":sync")
include(":feature:security:api")
include(":feature:security:impl")
include(":feature:design-app:api")
include(":feature:design-app:impl")
include(":feature:analysis:api")
include(":feature:analysis:impl")
include(":feature:history:impl")
include(":feature:history:api")
include(":feature:account:api")
include(":feature:account:impl")
include(":core:logging-error")
include(":feature:haptics:api")
include(":feature:haptics:impl")
include(":feature:sounds:api")
include(":feature:sounds:impl")
include(":feature:languages:api")
include(":feature:languages:impl")
include(":feature:synchronization:api")
include(":feature:synchronization:impl")
include(":feature:auth:api")
include(":feature:auth:impl")
include(":lint")
include(":core:common")
include(":core:workmanager")
include(":core:notifications")
