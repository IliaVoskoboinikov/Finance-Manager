plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.feature.category.categoryApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.data)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.core.domain)


}