plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.feature.category.category_impl"
}

dependencies {
    implementation(projects.feature.category.categoryApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.data)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.core.domain)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)

}