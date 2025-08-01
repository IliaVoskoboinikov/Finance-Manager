plugins {
    id("android-core-module")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "soft.divan.financemanager.core.data"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.androidx.datastore.preferences)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.converter.gson)

}