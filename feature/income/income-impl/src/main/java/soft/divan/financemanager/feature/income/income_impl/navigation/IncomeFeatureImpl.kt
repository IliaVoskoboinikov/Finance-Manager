package soft.divan.financemanager.feature.income.income_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.income.income_impl.presenter.screen.HistoryIncomeScreen
import soft.divan.financemanager.feature.income.income_impl.presenter.screen.IncomeScreen
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import javax.inject.Inject

private const val baseRoute = "income"
private const val scenarioIncomeRoute = "${baseRoute}/scenario"
private const val screenIncomeHistoryRoute = "$scenarioIncomeRoute/income_history"


class IncomeFeatureImpl @Inject constructor(
) : IncomeFeatureApi {

    override val route: String = baseRoute

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            IncomeScreen(
                modifier = modifier,
                onNavigateToHistory = {
                    navController.navigate(scenarioIncomeRoute)
                },
                navController = navController,
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scenarioIncomeRoute,
            startDestination = screenIncomeHistoryRoute
        ) {

            composable(route = screenIncomeHistoryRoute) {
                HistoryIncomeScreen(
                    modifier = modifier,
                    onNavigateBack = navController::popBackStack,
                    onNavigateToTransaction = { transitionId ->
                        navController.navigate(
                            transactionFeatureApi.transactionRouteWithArgs(
                                transitionId,
                                true
                            )
                        )
                    },
                )
            }

        }
    }
}