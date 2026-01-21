plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.transactionsToday.api)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.feature.transaction.api)
    implementation(projects.feature.history.api)
    implementation(projects.feature.haptics.api)
}