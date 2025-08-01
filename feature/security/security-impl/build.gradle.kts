plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.feature.security.security_impl"
}

dependencies {
    implementation(projects.feature.security.securityApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation("androidx.security:security-crypto:1.1.0-beta01")
    implementation("androidx.compose.material:material:1.6.5")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

}