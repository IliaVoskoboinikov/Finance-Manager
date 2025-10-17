import com.asarkar.gradle.buildtimetracker.BarPosition
import com.asarkar.gradle.buildtimetracker.Output
import com.asarkar.gradle.buildtimetracker.Sort
import java.time.Duration

plugins {
    alias(libs.plugins.soft.divan.android.app.module)
    alias(libs.plugins.soft.divan.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.graph)
    alias(libs.plugins.time.tracker)

}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = true
}

buildTimeTracker {
    barPosition = BarPosition.TRAILING
    sortBy = Sort.ASC
    output = Output.CSV
    minTaskDuration = Duration.ofSeconds(1)
    reportsDir.set(File(layout.buildDirectory.get().asFile, "reports/buildTimeTracker"))
}


android {
    namespace = Const.NAMESPACE
    defaultConfig {
        applicationId = Const.NAMESPACE
        versionCode = Const.VERSION_CODE
        versionName = Const.VERSION_NAME
        targetSdk = Const.COMPILE_SKD
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

    implementation(libs.androidx.lifecycle.process)
}