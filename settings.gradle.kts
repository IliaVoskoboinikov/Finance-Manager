pluginManagement {
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
include(":core:network")
include(":core:uikit")
include(":core:string")
include(":core:domain")
include(":core:data")
include(":feature:income")
include(":core:shared-history-transaction-category")
include(":feature:splash-screen")
include(":feature:settings:settings-api")
include(":feature:settings:settings-impl")
include(":core:feature-api")
include(":feature:income:income-api")
include(":feature:income:income-impl")
include(":feature:expenses:expenses-api")
include(":feature:expenses:expenses-impl")
include(":feature:category:category-api")
include(":feature:category:category-impl")
include(":feature:account:account-api")
include(":feature:account:account-impl")
include(":feature:transaction:transaction-api")
include(":feature:transaction:transaction-impl")
include(":core:database")
include(":feature:splash-screen:splash-screen-api")
include(":feature:splash-screen:splash-screen-impl")
include(":sync")
include(":feature:security:security-api")
include(":feature:security:security-impl")
