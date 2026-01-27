plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.splashScreen.api)
    implementation(projects.core.uikit)

    implementation(libs.lottie.compose)
}
