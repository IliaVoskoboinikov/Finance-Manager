plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.feature.transaction.transaction_impl"
}

dependencies {
    implementation(projects.feature.income.incomeApi)
    implementation(projects.core.database)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.feature.transaction.transactionApi)

    implementation(libs.converter.gson)
    implementation(libs.retrofit)
}