plugins {
    alias(libs.plugins.soft.divan.featureImpl.module)
}

dependencies {
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.colorpicker.compose)
    implementation(libs.androidx.datastore.core.android)
}