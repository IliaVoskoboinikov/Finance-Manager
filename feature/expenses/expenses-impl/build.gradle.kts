plugins {
    alias(libs.plugins.soft.divan.featureImpl.module)
}

dependencies {
    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.feature.transaction.transactionApi)
}