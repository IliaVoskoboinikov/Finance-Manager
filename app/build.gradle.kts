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
    implementation(projects.core.loggingError.loggingErrorApi)
    implementation(projects.core.loggingError.loggingErrorImpl)

    implementation(projects.feature.category.categoryApi)
    implementation(projects.feature.category.categoryImpl)

    implementation(projects.feature.settings.settingsApi)
    implementation(projects.feature.settings.settingsImpl)

    implementation(projects.feature.myAccounts.myAccountsApi)
    implementation(projects.feature.myAccounts.myAccountsImpl)

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

    implementation(projects.feature.account.accountApi)
    implementation(projects.feature.account.accountImpl)

    implementation(projects.feature.haptics.hapticsApi)
    implementation(projects.feature.haptics.hapticsImpl)

    implementation(projects.feature.sounds.soundsApi)
    implementation(projects.feature.sounds.soundsImpl)

    implementation(libs.androidx.lifecycle.process)
}