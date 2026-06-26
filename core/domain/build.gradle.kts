plugins {
    alias(libs.plugins.soft.divan.jvm.library)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
}
