package soft.divan.financemanager.feature.expenses.expenses_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.screen.ExpensesScreen
import soft.divan.financemanager.feature.expenses.expenses_impl.presenter.screen.HistoryExpensesScreen

import javax.inject.Inject

private const val baseRoute = "expenses"
private const val scenarioExpensesRoute = "${baseRoute}/scenario"
private const val screenExpensesHistoryRoute = "$scenarioExpensesRoute/income_history"

class ExpensesFeatureImpl @Inject constructor() : ExpensesFeatureApi {

    override val expensesRoute: String = baseRoute

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(expensesRoute) {
            ExpensesScreen(
                modifier = modifier,
                onNavigateToHistory = {
                    navController.navigate(scenarioExpensesRoute)
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
                    navController = navController
                )
            }

        }
    }
}