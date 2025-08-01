plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.financemanager.feature.category.category_api"
}

dependencies {
    api(projects.core.featureApi)
}