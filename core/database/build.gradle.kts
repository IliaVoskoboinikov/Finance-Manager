plugins {
    id("android-core-module")
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
}