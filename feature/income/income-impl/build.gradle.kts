plugins {
    alias(libs.plugins.soft.divan.featureImpl.module)
}

dependencies {
    implementation(projects.feature.income.incomeApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
}