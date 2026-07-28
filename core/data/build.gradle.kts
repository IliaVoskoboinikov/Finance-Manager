plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.loggingError)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.auth)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.converter.gson)
    implementation(libs.androidx.room.ktx)

    // todo
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
    // Robolectric-тест DataStore-провайдера (Context.dataStore в DataProviderModule)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
