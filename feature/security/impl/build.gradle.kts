plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

android {
    testOptions {
        unitTests {
            // Robolectric-тесту BiometricHelper нужны строковые ресурсы промпта
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.feature.security.api)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.security)

    implementation(libs.androidx.biometric.ktx)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
