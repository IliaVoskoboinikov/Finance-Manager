plugins {
    id("android-core-module")
    id("android-hilt")
}

android {
    namespace = "soft.divan.financemanager.core.data"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.converter.gson)

}