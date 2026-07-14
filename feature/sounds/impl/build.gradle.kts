plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

android {
    testOptions {
        unitTests {
            // Robolectric-тесту SoundsPoolHolder нужен доступ к raw-ресурсам (R.raw.coin)
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.feature.sounds.api)
    implementation(projects.core.common)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Robolectric-тест SoundsPoolHolder
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
