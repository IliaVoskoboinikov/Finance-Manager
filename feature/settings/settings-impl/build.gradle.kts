plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.settings.settingsApi)
    implementation(projects.core.uikit)
    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.feature.haptic.hapticApi)
}