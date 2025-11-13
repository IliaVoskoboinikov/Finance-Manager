plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.analysis.analysisApi)
    implementation(projects.core.uikit)
}