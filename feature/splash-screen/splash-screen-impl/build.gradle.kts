plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.core.string)
    implementation(projects.core.uikit)

    implementation(libs.lottie.compose)
}