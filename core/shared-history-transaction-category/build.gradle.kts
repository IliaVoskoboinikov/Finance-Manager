plugins {
    alias(libs.plugins.soft.divan.core.module)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))

}