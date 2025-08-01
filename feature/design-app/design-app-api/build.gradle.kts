plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.design_app.design_app_api"
}

dependencies {
    api(projects.core.featureApi)
}