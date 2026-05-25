plugins {
    alias(libs.plugins.soft.divan.feature.impl)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.feature.auth.api)
    implementation(projects.core.uikit)
    implementation(projects.core.loggingError)
    implementation(projects.core.auth)
    implementation(projects.core.domain)
    implementation(projects.sync)
}
