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
include(":feature:category")
include(":core:uikit")
include(":core:string")
include(":feature:account")
include(":core:domain")
include(":core:data")
include(":feature:expanses")
include(":feature:income")
include(":core:shared-history-transaction-category")
include(":feature:splash-screen")
include(":feature:settings:settings-api")
include(":feature:settings:settings-impl")
include(":core:feature-api")
