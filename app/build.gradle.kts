plugins {
    id("android-app-module")
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
    defaultConfig {
        applicationId = Const.NAMESPACE
        versionCode = 1
        versionName = "1.0"
        targetSdk = Const.COMPILE_SKD
    }
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.sync)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.string)
    implementation(projects.core.domain)
    implementation(projects.core.sharedHistoryTransactionCategory)
    implementation(projects.core.data)

    implementation(projects.feature.category.categoryApi)
    implementation(projects.feature.category.categoryImpl)

    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.settings.settingsImpl)

    implementation(projects.feature.account.accountApi)
    implementation(projects.feature.account.accountImpl)

    implementation(projects.feature.income.incomeApi)
    implementation(projects.feature.income.incomeImpl)

    implementation(projects.feature.expenses.expensesApi)
    implementation(projects.feature.expenses.expensesImpl)

    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.splashScreen.splashScreenImpl)

    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.feature.transaction.transactionImpl)

    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.security.securityImpl)

    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.feature.designApp.designAppImpl)


    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.process)
}