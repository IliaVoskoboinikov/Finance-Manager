plugins {
    id("android-core-module")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))
    //todo протом удалить     implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.compose)
}