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
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.sync)
    implementation(projects.core.network)
    implementation(projects.core.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.loggingError.api)
    implementation(projects.core.loggingError.impl)

    implementation(projects.feature.category.api)
    implementation(projects.feature.category.impl)

    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)

    implementation(projects.feature.myAccounts.api)
    implementation(projects.feature.myAccounts.impl)

    implementation(projects.feature.transactionsToday.api)
    implementation(projects.feature.transactionsToday.impl)

    implementation(projects.feature.splashScreen.api)
    implementation(projects.feature.splashScreen.impl)

    implementation(projects.feature.transaction.api)
    implementation(projects.feature.transaction.impl)

    implementation(projects.feature.security.api)
    implementation(projects.feature.security.impl)

    implementation(projects.feature.designApp.api)
    implementation(projects.feature.designApp.impl)

    implementation(projects.feature.analysis.api)
    implementation(projects.feature.analysis.impl)

    implementation(projects.feature.history.api)
    implementation(projects.feature.history.impl)

    implementation(projects.feature.account.api)
    implementation(projects.feature.account.impl)

    implementation(projects.feature.haptics.api)
    implementation(projects.feature.haptics.impl)

    implementation(projects.feature.sounds.api)
    implementation(projects.feature.sounds.impl)

    implementation(projects.feature.languages.api)
    implementation(projects.feature.languages.impl)

    implementation(projects.feature.synchronization.api)
    implementation(projects.feature.synchronization.impl)

    implementation(libs.androidx.lifecycle.process)
}