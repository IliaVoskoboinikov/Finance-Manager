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

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.converter.gson)

    // todo
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
}
