plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.security.security_api"
}

dependencies {
    api(projects.core.featureApi)
}