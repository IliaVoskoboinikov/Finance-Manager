plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.category.categoryApi)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.data)
    implementation(projects.core.domain)


}