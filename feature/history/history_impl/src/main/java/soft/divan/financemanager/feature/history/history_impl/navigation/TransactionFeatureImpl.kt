package soft.divan.financemanager.feature.history.history_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.history.history_api.HistoryFeatureApi
import soft.divan.financemanager.feature.history.history_impl.precenter.screens.HistoryScreen
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import javax.inject.Inject


private const val isIncomeKey: String = "isIncome"

class HistoryFeatureImpl @Inject constructor() : HistoryFeatureApi {

    override val route: String = "history"

    @Inject
    lateinit var transactionFeatureApi: TransactionFeatureApi

    @Inject
    lateinit var analysisFeatureApi: AnalysisFeatureApi

    override fun transactionRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            "${route}/{$isIncomeKey}",
            arguments = listOf(
                navArgument(isIncomeKey) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val isIncome = backStackEntry.arguments?.getBoolean(isIncomeKey) ?: false

            HistoryScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateBack = navController::popBackStack,
                onNavigateToTransaction = { transitionId ->
                    navController.navigate(
                        transactionFeatureApi.transactionRouteWithArgs(
                            transactionId = transitionId,
                            isIncome = isIncome
                        )
                    )
                },
                onNavigateToAnalysis = {
                    navController.navigate(analysisFeatureApi.analysisRouteWithArgs(isIncome = isIncome))
                }
            )
        }
    }


}