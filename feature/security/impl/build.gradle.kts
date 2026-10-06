plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

android {
    testOptions {
        unitTests {
            // Robolectric-тестам нужны строковые ресурсы и DataStore настроек
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.feature.security.api)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.security)
    // Биометрический шлюз хранилища ключей (шифр для CryptoObject) — только в :core:data
    implementation(projects.core.data)
    // Гость или залогинен: от этого зависят тексты предупреждений о стирании
    implementation(projects.core.auth)

    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
