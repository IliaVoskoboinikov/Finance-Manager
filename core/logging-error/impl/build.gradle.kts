plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.loggingError.api)
    implementation(platform(libs.bom))
    implementation(libs.crashlytics)
}