plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(projects.core.uikit)
    implementation(projects.feature.security.api)
    implementation(projects.feature.designApp.api)
    implementation(projects.feature.haptics.api)
    implementation(projects.feature.sounds.api)
    implementation(projects.feature.languages.api)
    implementation(projects.feature.synchronization.api)
}
