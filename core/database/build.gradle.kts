plugins {
    id("android-core-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.finansemanager.core.database"
}

dependencies {
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
}