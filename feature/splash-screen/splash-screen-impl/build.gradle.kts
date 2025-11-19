plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.transactionsToday.transactionsTodayApi)
    implementation(projects.core.uikit)

    implementation(libs.lottie.compose)
}