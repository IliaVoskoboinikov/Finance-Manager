plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.account.account_impl"
}

dependencies {
    api(projects.core.featureApi)
}