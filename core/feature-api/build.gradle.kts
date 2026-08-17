plugins {
    alias(libs.plugins.soft.divan.core)
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.ui)
}
