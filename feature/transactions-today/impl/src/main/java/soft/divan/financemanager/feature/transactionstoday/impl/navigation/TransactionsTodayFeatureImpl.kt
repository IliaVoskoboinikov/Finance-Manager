package soft.divan.financemanager.feature.transactionstoday.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.screen.TransactionsTodayScreen
import javax.inject.Inject

private const val BASE_ROURE = "transactionsToday"
private const val IS_INCOME_KEY: String = "isIncome"

class TransactionsTodayFeatureImpl @Inject constructor() : TransactionsTodayFeatureApi {

    override val route: String = BASE_ROURE
    override val route2: String = BASE_ROURE + "2"

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    @Inject
    lateinit var historyFeatureApi: HistoryFeatureApi

    override fun transactionsTodayRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = "$route/{$IS_INCOME_KEY}",
            arguments = listOf(
                navArgument(IS_INCOME_KEY) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isIncome = backStackEntry.arguments?.getBoolean(IS_INCOME_KEY) ?: false

            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateToHistory = {
                    navController.navigate(historyFeatureApi.transactionRouteWithArgs(isIncome = isIncome))
                },
                onNavigateToNewTransaction = {
                    navController.navigate(transactionFeatureApi.transactionRouteWithArgs(isIncome))
                },
                onNavigateToOldTransaction = { idTransition ->
                    navController.navigate(
                        transactionFeatureApi.transactionRouteWithArgs(
                            transactionId = idTransition,
                            isIncome = isIncome
                        )
                    )
                },
            )
        }

        // EXPENSES
        navGraphBuilder.composable(
            route = route,

            ) {
            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = false,
                onNavigateToHistory = {
                    navController.navigate(historyFeatureApi.transactionRouteWithArgs(isIncome = false))
                },
                onNavigateToNewTransaction = {
                    navController.navigate(transactionFeatureApi.transactionRouteWithArgs(false))
                },
                onNavigateToOldTransaction = { idTransition ->
                    navController.navigate(
                        transactionFeatureApi.transactionRouteWithArgs(
                            transactionId = idTransition,
                            isIncome = false
                        )
                    )
                },
            )
        }

        // INCOME
        navGraphBuilder.composable(
            route = route2,

            ) {
            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = true,
                onNavigateToHistory = {
                    navController.navigate(historyFeatureApi.transactionRouteWithArgs(isIncome = true))
                },
                onNavigateToNewTransaction = {
                    navController.navigate(transactionFeatureApi.transactionRouteWithArgs(true))
                },
                onNavigateToOldTransaction = { idTransition ->
                    navController.navigate(
                        transactionFeatureApi.transactionRouteWithArgs(
                            transactionId = idTransition,
                            isIncome = true
                        )
                    )
                },
            )
        }
    }
}
