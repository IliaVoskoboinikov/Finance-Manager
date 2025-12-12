plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
    alias(libs.plugins.gms)
}

dependencies {
    implementation(projects.core.logging)
    implementation(platform(libs.bom))
    implementation(libs.crashlytics)

}