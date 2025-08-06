plugins {
    id("android-core-module")
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.core.data)

    ksp(libs.hilt.ext.compiler)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.hilt.ext.work)
    implementation(libs.androidx.work.ktx)
}