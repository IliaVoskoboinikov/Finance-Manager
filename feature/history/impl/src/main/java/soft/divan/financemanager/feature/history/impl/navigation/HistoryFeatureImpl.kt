package soft.divan.financemanager.feature.history.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.analysis.api.AnalysisFeatureApi
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.history.impl.precenter.screens.HistoryScreen
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import javax.inject.Inject

private const val BASE_ROUTE = "history"
const val IS_INCOME_KEY: String = "isIncome"

class HistoryFeatureImpl @Inject constructor() : HistoryFeatureApi {

    override val route: String = BASE_ROUTE

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    @Inject
    lateinit var analysisFeatureApi: AnalysisFeatureApi

    override fun transactionRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            "${scope.route()}/{$IS_INCOME_KEY}",
            arguments = listOf(
                navArgument(IS_INCOME_KEY) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val isIncome = backStackEntry.arguments?.getBoolean(IS_INCOME_KEY) ?: false

            HistoryScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack,
                onNavigateToTransaction = { transitionId ->
                    navController.navigate(
                        scope.route(
                            transactionFeatureApi.transactionRouteWithArgs(
                                transactionId = transitionId,
                                isIncome = isIncome
                            )
                        )
                    )
                },
                onNavigateToAnalysis = {
                    navController.navigate(scope.route(analysisFeatureApi.analysisRouteWithArgs(isIncome = isIncome)))
                }
            )
        }
        transactionFeatureApi.registerGraph(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope.child(transactionFeatureApi.route),
            modifier = modifier
        )

        analysisFeatureApi.registerGraph(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope.child(analysisFeatureApi.route),
            modifier = modifier
        )
    }


}