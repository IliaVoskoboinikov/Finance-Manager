plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.designApp.api)
    implementation(projects.core.uikit)
    implementation(projects.feature.haptics.api)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.colorpicker.compose)
    implementation(libs.androidx.datastore.core)
}