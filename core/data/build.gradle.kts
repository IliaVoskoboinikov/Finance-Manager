plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.loggingError.api)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.converter.gson)
}