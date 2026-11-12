plugins {
    alias(libs.plugins.soft.divan.core)
    alias(libs.plugins.soft.divan.hilt)
}

android {
    // Имена исключений — в release-сборке приложения (см. consumer-rules.pro)
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    // BuildConfig.DEBUG: полный текст исключений в logcat — только в debug-сборке
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.crashlytics)
}
