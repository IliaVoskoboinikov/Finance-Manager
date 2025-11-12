plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
}