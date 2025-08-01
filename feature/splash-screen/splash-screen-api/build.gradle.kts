plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.splash_screen.splash_screen_api"
}

dependencies {
    api(projects.core.featureApi)
}