package soft.divan.financemanager.feature.transactionstoday.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.screen.TransactionsTodayScreen
import javax.inject.Inject

private const val BASE_ROUTE = "transactions_today"

class TransactionsTodayFeatureImpl @Inject constructor() : TransactionsTodayFeatureApi {

    override val route: String = "$BASE_ROUTE/expense"
    override val expenseRoute: String = "$BASE_ROUTE/expense"
    override val incomeRoute: String = "$BASE_ROUTE/income"

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    @Inject
    lateinit var historyFeatureApi: HistoryFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            val isIncome = scope.route().endsWith("income")

            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateToHistory = {
                    navController.navigate(
                        scope.route(
                            historyFeatureApi.transactionRouteWithArgs(
                                isIncome = isIncome
                            )
                        )
                    )
                },
                onNavigateToNewTransaction = {
                    navController.navigate(
                        scope.route(
                            transactionFeatureApi.transactionRouteWithArgs(
                                isIncome
                            )
                        )
                    )
                },
                onNavigateToOldTransaction = { idTransition ->
                    navController.navigate(
                        scope.route(
                            transactionFeatureApi.transactionRouteWithArgs(
                                transactionId = idTransition,
                                isIncome = isIncome
                            )
                        )
                    )
                }
            )
        }

        transactionFeatureApi.registerGraph(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope.child(transactionFeatureApi.route),
            modifier = modifier
        )

        historyFeatureApi.registerGraph(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope.child(historyFeatureApi.route),
            modifier = modifier
        )
    }
}
