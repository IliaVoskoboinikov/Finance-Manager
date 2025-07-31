package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import soft.divan.financemanager.core.feature_api.register
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    splashScreenFeatureApi: SplashScreenFeatureApi,
    expenseFeatureApi: ExpensesFeatureApi,
    incomeFeatureApi: IncomeFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    accountFeatureApi: AccountFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    transactionFeatureApi: TransactionFeatureApi,
    securityFeatureApi: SecurityFeatureApi,
    designAppFeatureApi: DesignAppFeatureApi
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
        register(featureApi = incomeFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = expenseFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = categoryFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = accountFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = transactionFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = securityFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = designAppFeatureApi, navController = navController, modifier = modifier)
    }
}