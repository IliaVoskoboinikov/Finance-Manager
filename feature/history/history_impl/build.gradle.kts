plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.uikit)
    implementation(projects.feature.history.historyApi)
    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.feature.analysis.analysisApi)

}