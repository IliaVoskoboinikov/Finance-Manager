plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.feature.design_app.design_app_impl"
}

dependencies {
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    implementation(libs.androidx.datastore.core.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

}