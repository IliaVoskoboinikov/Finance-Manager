plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Robolectric-тесты DAO на in-memory Room
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
