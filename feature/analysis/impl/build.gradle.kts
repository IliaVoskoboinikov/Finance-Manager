plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.analysis.api)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(libs.ycharts)

    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
}
