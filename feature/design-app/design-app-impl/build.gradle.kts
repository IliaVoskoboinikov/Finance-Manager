plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.uikit)
    implementation(projects.feature.haptics.hapticsApi)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.colorpicker.compose)
    implementation(libs.androidx.datastore.core)
}