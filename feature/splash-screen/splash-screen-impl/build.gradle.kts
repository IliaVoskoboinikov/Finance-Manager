plugins {
    alias(libs.plugins.soft.divan.featureImpl.module)
}

dependencies {
    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.core.string)
    implementation(projects.core.uikit)

    implementation(libs.lottie.compose)
}