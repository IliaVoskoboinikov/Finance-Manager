package soft.divan.financemanager.feature.transaction.transaction_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.screens.TransactionScreen
import javax.inject.Inject


private const val transactionIdKey: String = "transactionId"
private const val isIncomeKey: String = "isIncome"

class TransactionFeatureImpl @Inject constructor() : TransactionFeatureApi {

    override val route: String = "transaction"

    override fun transactionRouteWithArgs(transactionId: Int, isIncome: Boolean) =
        "$route/$transactionId/$isIncome"

    override fun transactionRouteWithArgs(isIncome: Boolean) = "$route/$isIncome"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = "${route}/{$transactionIdKey}/{$isIncomeKey}",
            arguments = listOf(
                navArgument(transactionIdKey) {
                    type = NavType.IntType
                },
                navArgument(isIncomeKey) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->

            val transactionId = backStackEntry.arguments?.getInt(transactionIdKey)
            val isIncome = backStackEntry.arguments?.getBoolean(isIncomeKey) ?: false

            TransactionScreen(
                modifier = modifier,
                transactionId = transactionId,
                isIncome = isIncome,
                onNavigateBack = navController::popBackStack
            )

        }

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

            TransactionScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateBack = navController::popBackStack
            )
        }
    }


}