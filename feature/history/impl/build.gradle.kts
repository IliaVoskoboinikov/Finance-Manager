plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.uikit)
    implementation(projects.feature.history.api)
    implementation(projects.feature.transaction.api)
    implementation(projects.feature.analysis.api)

}