package soft.divan.financemanager.feature.analysis.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.feature.analysis.api.AnalysisFeatureApi
import soft.divan.financemanager.feature.analysis.impl.precenter.screen.AnalysisScreen
import javax.inject.Inject

private const val BASE_ROUTE = "analysis"
const val IS_INCOME_KEY: String = "isIncome"

class AnalysisFeatureImpl @Inject constructor() : AnalysisFeatureApi {

    override val route: String = BASE_ROUTE

    override fun analysisRouteWithArgs(isIncome: Boolean): String {
        val income = isIncome.toString()
        return "$route/$income"
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = "${route}/{$IS_INCOME_KEY}",
            arguments = listOf(
                navArgument(IS_INCOME_KEY) {
                    type = NavType.BoolType
                }
            )
        ) {
            AnalysisScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}