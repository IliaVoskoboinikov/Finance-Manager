plugins {
    id("android-core-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.sync"
}

dependencies {
    implementation(projects.core.data)

    ksp(libs.hilt.compiler)
    ksp(libs.hilt.ext.compiler)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.hilt.ext.work)
    implementation(libs.hilt.android)
    implementation(libs.androidx.work.ktx)
}