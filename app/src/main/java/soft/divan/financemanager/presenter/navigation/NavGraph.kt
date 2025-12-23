package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import soft.divan.financemanager.core.feature_api.register
import soft.divan.financemanager.feature.account.account_api.AccountFeatureApi
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.haptic.haptic_api.HapticFeatureApi
import soft.divan.financemanager.feature.history.history_api.HistoryFeatureApi
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactions_today.transactions_today_api.TransactionsTodayFeatureApi

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    splashScreenFeatureApi: SplashScreenFeatureApi,
    transactionsTodayFeatureApi: TransactionsTodayFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    accountFeatureApi: AccountFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    transactionFeatureApi: TransactionFeatureApi,
    securityFeatureApi: SecurityFeatureApi,
    designAppFeatureApi: DesignAppFeatureApi,
    analysisFeatureApi: AnalysisFeatureApi,
    historyFeatureApi: HistoryFeatureApi,
    myAccountsFeatureApi: MyAccountsFeatureApi,
    hapticFeatureApi: HapticFeatureApi,
) {
    NavHost(
        navController = navController,
        startDestination = splashScreenFeatureApi.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        register(featureApi = splashScreenFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = settingsFeatureApi, navController = navController, modifier = modifier)
        register(
            featureApi = transactionsTodayFeatureApi,
            navController = navController,
            modifier = modifier
        )
        register(featureApi = categoryFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = accountFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = transactionFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = securityFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = designAppFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = analysisFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = historyFeatureApi, navController = navController, modifier = modifier)
        register(
            featureApi = myAccountsFeatureApi, navController = navController, modifier = modifier
        )
        register(featureApi = hapticFeatureApi, navController = navController, modifier = modifier)
    }
}