plugins {
    id("android-core-module")
    id("android-hilt")
}

dependencies {
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
}