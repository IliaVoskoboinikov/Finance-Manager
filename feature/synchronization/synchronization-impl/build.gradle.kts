plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.synchronization.synchronizationApi)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.sync)
}