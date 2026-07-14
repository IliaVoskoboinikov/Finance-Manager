plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.haptics.api)
    implementation(projects.core.uikit)
    implementation(projects.core.common)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Robolectric-тест HapticsModule.provideVibrator (системный Vibrator/VibratorManager)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
