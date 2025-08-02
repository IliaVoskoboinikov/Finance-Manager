plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.feature.settings.settings_impl"

}

dependencies {
    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.string)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core.android)
}