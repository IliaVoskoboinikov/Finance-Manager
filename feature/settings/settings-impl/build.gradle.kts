plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.soft.divan.hilt)
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