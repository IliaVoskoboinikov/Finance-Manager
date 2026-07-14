plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.feature.designApp.api)
    implementation(projects.core.uikit)
    implementation(projects.feature.haptics.api)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.colorpicker.compose)
    implementation(libs.androidx.datastore.core)

    // Robolectric-тест DataStore-провайдера (Context.themeDataStore)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
