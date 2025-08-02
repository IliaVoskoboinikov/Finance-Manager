plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.feature.income.income_impl"

}

dependencies {
    implementation(projects.feature.income.incomeApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
}