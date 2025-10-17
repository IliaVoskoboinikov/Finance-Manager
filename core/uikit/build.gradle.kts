plugins {
    alias(libs.plugins.soft.divan.core.module)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    api(libs.androidx.material.icons.extended)

}