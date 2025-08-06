plugins {
    id("android-featureImpl-module")
    alias(libs.plugins.soft.divan.hilt)
}

dependencies {
    implementation(projects.feature.account.accountApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)


}