plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.crashlytics)
}
