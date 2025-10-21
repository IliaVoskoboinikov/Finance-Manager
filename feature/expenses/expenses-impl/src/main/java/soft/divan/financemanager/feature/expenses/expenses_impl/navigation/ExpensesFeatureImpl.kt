package soft.divan.financemanager.feature.expenses.expenses_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.screen.ExpensesScreen
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.screen.HistoryExpensesScreen
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import javax.inject.Inject

private const val baseRoute = "expenses"
private const val scenarioExpensesRoute = "${baseRoute}/scenario"
private const val screenExpensesHistoryRoute = "$scenarioExpensesRoute/expenses_history"

class ExpensesFeatureImpl @Inject constructor() : ExpensesFeatureApi {

    override val route: String = baseRoute

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            ExpensesScreen(
                modifier = modifier,
                onNavigateToHistory = {
                    navController.navigate(scenarioExpensesRoute)
                },
                onNavigateToNewTransaction = {
                    navController.navigate(transactionFeatureApi.route)
                },
                onNavigateToOldTransaction = { transitionId ->
                    navController.navigate(
                        transactionFeatureApi.transactionRouteWithArgs(
                            transitionId,
                            false
                        )
                    )
                },
                navController = navController,
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scenarioExpensesRoute,
            startDestination = screenExpensesHistoryRoute
        ) {

            composable(route = screenExpensesHistoryRoute) {
                HistoryExpensesScreen(
                    modifier = modifier,
                    onNavigateBack = navController::popBackStack,
                    onNavigateToTransaction = { transitionId ->
                        navController.navigate(
                            transactionFeatureApi.transactionRouteWithArgs(
                                transitionId,
                                false
                            )
                        )
                    },
                )
            }

        }
    }
}