plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.feature.splash_screen.splash_screen_impl"
}

dependencies {
    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.core.string)
    implementation(projects.core.uikit)

    implementation(libs.lottie.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

}