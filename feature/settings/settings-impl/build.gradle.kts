plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.settings.settingsApi)
    implementation(projects.core.uikit)
    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.feature.haptics.hapticsApi)
    implementation(projects.feature.sounds.soundsApi)
    implementation(projects.feature.languages.languagesApi)
    implementation(projects.feature.synchronization.synchronizationApi)
}