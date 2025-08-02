plugins {
    id("android-core-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.sync"
}

dependencies {
    implementation(projects.core.data)

    ksp(libs.hilt.ext.compiler)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.hilt.ext.work)
    implementation(libs.androidx.work.ktx)
}