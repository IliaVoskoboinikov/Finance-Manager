package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.feature_api.register
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.presenter.screens.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    expenseFeatureApi: ExpensesFeatureApi,
    incomeFeatureApi: IncomeFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    accountFeatureApi: AccountFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    transactionFeatureApi: TransactionFeatureApi

) {
    NavHost(
        navController = navController,
        startDestination = SplashScreen.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        register(featureApi = settingsFeatureApi, navController = navController, modifier = modifier)
        composable(SplashScreen.route) { SplashScreen(navController) }
        register(featureApi = incomeFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = expenseFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = categoryFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = accountFeatureApi, navController = navController, modifier = modifier)
        register(featureApi = transactionFeatureApi, navController = navController, modifier = modifier)
    }
}