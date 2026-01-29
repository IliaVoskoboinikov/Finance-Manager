package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.core.featureapi.register
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    transactionsTodayFeatureApi: TransactionsTodayFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    myAccountsFeatureApi: MyAccountsFeatureApi,
) {
    NavHost(
        navController = navController,
        startDestination = transactionsTodayFeatureApi.expenseRoute,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        register(
            featureApi = transactionsTodayFeatureApi,
            navController = navController,
            scope = RouteScope(transactionsTodayFeatureApi.expenseRoute),
            modifier = modifier
        )

        register(
            featureApi = transactionsTodayFeatureApi,
            navController = navController,
            scope = RouteScope(transactionsTodayFeatureApi.incomeRoute),
            modifier = modifier
        )

        register(
            featureApi = myAccountsFeatureApi,
            navController = navController,
            scope = RouteScope(myAccountsFeatureApi.route),
            modifier = modifier
        )

        register(
            featureApi = categoryFeatureApi,
            navController = navController,
            scope = RouteScope(categoryFeatureApi.route),
            modifier = modifier
        )

        register(
            featureApi = settingsFeatureApi,
            navController = navController,
            modifier = modifier,
            scope = RouteScope(settingsFeatureApi.route)
        )
    }
}