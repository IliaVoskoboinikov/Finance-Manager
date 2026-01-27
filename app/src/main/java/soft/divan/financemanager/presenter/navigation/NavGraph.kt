package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import soft.divan.financemanager.core.featureapi.register
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.analysis.api.AnalysisFeatureApi
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.designapp.api.DesignAppFeatureApi
import soft.divan.financemanager.feature.haptics.api.HapticsFeatureApi
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.sounds.api.SoundsFeatureApi
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
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
    hapticsFeatureApi: HapticsFeatureApi,
    soundsFeatureApi: SoundsFeatureApi,
    languagesFeatureApi: LanguagesFeatureApi,
    synchronizationFeatureApi: SynchronizationFeatureApi
) {
    NavHost(
        navController = navController,
        startDestination = transactionsTodayFeatureApi.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
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
        register(featureApi = hapticsFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = soundsFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = languagesFeatureApi, navController = navController, modifier = modifier)
        register(
            featureApi = synchronizationFeatureApi,
            navController = navController,
            modifier = modifier
        )
    }
}