plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.myAccounts.myAccountsApi)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.feature.account.accountApi)
    implementation(projects.feature.haptics.hapticsApi)

}