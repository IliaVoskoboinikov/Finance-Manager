plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
}