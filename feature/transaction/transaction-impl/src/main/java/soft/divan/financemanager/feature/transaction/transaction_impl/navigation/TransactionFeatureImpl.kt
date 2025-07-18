package soft.divan.financemanager.feature.transaction.transaction_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.screens.TransactionScreen
import javax.inject.Inject

class TransactionFeatureImpl @Inject constructor() : TransactionFeatureApi {
    override val transactionRoute: String = "transaction"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(transactionRoute) {
            TransactionScreen(
                modifier = modifier,
                navController = navController
            )
        }
    }
}