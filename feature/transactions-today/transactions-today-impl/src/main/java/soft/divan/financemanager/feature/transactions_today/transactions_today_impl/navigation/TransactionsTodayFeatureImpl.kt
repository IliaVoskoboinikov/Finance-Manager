package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactions_today.transactions_today_api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.screen.HistoryExpensesScreen
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.screen.TransactionsTodayScreen
import javax.inject.Inject

private const val baseRoute = "transactionsToday"
private const val scenarioExpensesRoute = "${baseRoute}/scenario"
private const val screenExpensesHistoryRoute = "$scenarioExpensesRoute/expenses_history"
private const val isIncomeKey: String = "isIncome"

class TransactionsTodayFeatureImpl @Inject constructor() : TransactionsTodayFeatureApi {

    override val route: String = baseRoute
    override val route2: String = baseRoute + "2"


    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    @Inject
    lateinit var analysisFeatureApi: AnalysisFeatureApi

    override fun transactionsTodayRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = "${route}/{$isIncomeKey}",
            arguments = listOf(navArgument(isIncomeKey) {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val isIncome = backStackEntry.arguments?.getBoolean(isIncomeKey) ?: false

            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateToHistory = {
                    navController.navigate(scenarioExpensesRoute)
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
                    navController.navigate(scenarioExpensesRoute)
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
                    navController.navigate(scenarioExpensesRoute)
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
                    onNavigateToAnalysis = {
                        navController.navigate(analysisFeatureApi.analysisRouteWithArgs(isIncome = false))
                    }
                )
            }

        }
    }

}