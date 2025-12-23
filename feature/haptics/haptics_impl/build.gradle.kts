plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.haptics.hapticsApi)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
}