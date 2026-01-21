plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.synchronization.api)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.sync)
}