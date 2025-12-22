plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.uikit)
}