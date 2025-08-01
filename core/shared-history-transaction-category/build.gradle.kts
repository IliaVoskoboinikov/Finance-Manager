plugins {
    id("android-core-module")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.core.shared_history_transaction_category"
}

dependencies {
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)

    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))
    //todo протом удалить     implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

}