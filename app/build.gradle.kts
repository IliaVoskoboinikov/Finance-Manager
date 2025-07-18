plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.graph)

}



detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = true

}

android {
    namespace = "soft.divan.financemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "soft.divan.financemanager"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.feature.category.categoryApi)
    implementation(projects.feature.category.categoryImpl)

    implementation(projects.core.string)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.settings.settingsImpl)

    implementation(projects.feature.account.accountApi)
    implementation(projects.feature.account.accountImpl)
    implementation(projects.feature.income.incomeApi)
    implementation(projects.feature.income.incomeImpl)

    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.feature.expenses.expensesImpl)

    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.feature.transaction.transactionImpl)

    implementation(libs.androidx.core.ktx)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.layout.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.lottie.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.retrofit)

    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)
}