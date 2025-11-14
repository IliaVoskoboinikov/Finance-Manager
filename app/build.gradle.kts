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

    implementation(projects.feature.analysis.analysisApi)
    implementation(projects.feature.analysis.analysisImpl)

    implementation(libs.androidx.lifecycle.process)
}