plugins {
    id("android-featureImpl-module")
    id("android-hilt")
}

dependencies {
    implementation(projects.feature.account.accountApi)
    implementation(projects.core.string)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)


}