plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.income.income_api"
}

dependencies {
    api(projects.core.featureApi)
}