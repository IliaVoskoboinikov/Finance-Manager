plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.feature.design_app.design_app_impl"
}

dependencies {
    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.core.uikit)

    implementation(libs.androidx.datastore.preferences)
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    implementation(libs.androidx.datastore.core.android)
}