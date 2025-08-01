plugins {
    id("android-featureApi-module")

}

android {
    namespace = "soft.divan.financemanager.feature.transaction.ransaction_api"
}

dependencies {
    api(projects.core.featureApi)
}