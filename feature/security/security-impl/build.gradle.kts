plugins {
    alias(libs.plugins.soft.divan.featureImpl.module)
}

dependencies {
    implementation(projects.feature.security.securityApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric.ktx)
}