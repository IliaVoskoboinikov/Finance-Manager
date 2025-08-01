plugins {
    id("android-featureApi-module")
}

android {
    namespace = "soft.divan.finansemanager.account"
}

dependencies {
    api(projects.core.featureApi)
}
