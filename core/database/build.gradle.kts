plugins {
    id("android-core-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.finansemanager.core.database"
}

dependencies {
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
}