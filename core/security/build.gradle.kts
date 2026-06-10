plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
