plugins {
    id("android-core-module")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "soft.divan.financemanager.uikit"
}

dependencies {
    implementation(projects.core.string)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    api(libs.androidx.material.icons.extended)

}