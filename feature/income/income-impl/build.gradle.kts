plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.income.incomeApi)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.feature.transaction.transactionApi)
}