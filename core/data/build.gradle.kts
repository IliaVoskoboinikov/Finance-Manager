plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

android {
    // Сквозной инструментальный тест хранилища ключей — на настоящих SQLCipher и Keystore
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.loggingError)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.auth)
    implementation(projects.core.security)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.converter.gson)
    implementation(libs.androidx.room.ktx)

    // Robolectric-тест DataStore-провайдера (Context.dataStore в DataProviderModule)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
