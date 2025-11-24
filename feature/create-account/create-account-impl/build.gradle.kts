plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.createAccount.createAccountApi)
    implementation(projects.core.domain)
    implementation(projects.core.uikit)
}