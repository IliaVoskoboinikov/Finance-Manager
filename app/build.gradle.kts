plugins {
    alias(libs.plugins.soft.divan.android.app)
}

android {
    namespace = Const.NAMESPACE
    defaultConfig {
        applicationId = Const.NAMESPACE
    }
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.sync)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)

    implementation(projects.feature.category.categoryApi)
    implementation(projects.feature.category.categoryImpl)

    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.settings.settingsImpl)

    implementation(projects.feature.account.accountApi)
    implementation(projects.feature.account.accountImpl)

    implementation(projects.feature.transactionsToday.transactionsTodayApi)
    implementation(projects.feature.transactionsToday.transactionsTodayImpl)

    implementation(projects.feature.splashScreen.splashScreenApi)
    implementation(projects.feature.splashScreen.splashScreenImpl)

    implementation(projects.feature.transaction.transactionApi)
    implementation(projects.feature.transaction.transactionImpl)

    implementation(projects.feature.security.securityApi)
    implementation(projects.feature.security.securityImpl)

    implementation(projects.feature.designApp.designAppApi)
    implementation(projects.feature.designApp.designAppImpl)

    implementation(projects.feature.analysis.analysisApi)
    implementation(projects.feature.analysis.analysisImpl)

    implementation(projects.feature.history.historyApi)
    implementation(projects.feature.history.historyImpl)

    implementation(projects.feature.createAccount.createAccountApi)
    implementation(projects.feature.createAccount.createAccountImpl)

    implementation(libs.androidx.lifecycle.process)
}