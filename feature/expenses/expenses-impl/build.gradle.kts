plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.feature.expenses.expenses_impl"
}

dependencies {
    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.feature.transaction.transactionApi)
}