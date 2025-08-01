plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
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
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.core.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

}