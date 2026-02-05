plugins {
    alias(libs.plugins.soft.divan.feature.impl)
}

dependencies {
    implementation(projects.feature.transaction.api)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.uikit)
    implementation(projects.feature.haptics.api)
    implementation(projects.feature.sounds.api)
}
