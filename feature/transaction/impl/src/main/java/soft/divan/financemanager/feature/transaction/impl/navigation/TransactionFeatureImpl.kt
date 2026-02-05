package soft.divan.financemanager.feature.transaction.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.impl.precenter.screens.TransactionScreen
import javax.inject.Inject

const val TRANSACTION_ID_KEY: String = "transactionId"
const val IS_INCOME_KEY: String = "isIncome"

class TransactionFeatureImpl @Inject constructor() : TransactionFeatureApi {

    override val route: String = "transaction"

    override fun transactionRouteWithArgs(transactionId: String, isIncome: Boolean) =
        "$route/$transactionId/$isIncome"

    override fun transactionRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = "${scope.route()}/{$TRANSACTION_ID_KEY}/{$IS_INCOME_KEY}",
            arguments = listOf(
                navArgument(TRANSACTION_ID_KEY) {
                    type = NavType.StringType
                },
                navArgument(IS_INCOME_KEY) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val isIncome = backStackEntry.arguments?.getBoolean(IS_INCOME_KEY) ?: false

            TransactionScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateBack = navController::popBackStack
            )
        }

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

            TransactionScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}
