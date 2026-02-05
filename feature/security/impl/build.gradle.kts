plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.security.api)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric.ktx)
}
