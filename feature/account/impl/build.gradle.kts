plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.account.api)
    implementation(projects.core.domain)
    implementation(projects.core.uikit)
    implementation(projects.feature.haptics.api)
}
