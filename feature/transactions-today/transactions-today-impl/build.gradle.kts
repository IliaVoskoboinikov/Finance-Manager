plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.transactionsToday.transactionsTodayApi)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.feature.history.historyApi)
    implementation(projects.feature.haptic.hapticApi)
}