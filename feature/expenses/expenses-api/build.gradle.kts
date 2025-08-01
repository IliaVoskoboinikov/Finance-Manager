plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.expenses.expeanses_api"
}

dependencies {
    api(projects.core.featureApi)
}