package soft.divan.financemanager.feature.account.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jakarta.inject.Inject
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.account.impl.precenter.screens.CreateAccountScreenScreen

private const val baseRoute = "account"
const val ACCOUNT_ID_KEY: String = "accountId"

class AccountFeatureImpl @Inject constructor() : AccountFeatureApi {

    override val route: String = baseRoute

    override fun accountRouteWithArgs(accountId: String) = "$route/$accountId"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {

        navGraphBuilder.composable(route) {
            CreateAccountScreenScreen(
                modifier = modifier,
                accountId = null,
                onNavigateBack = navController::popBackStack
            )
        }

        navGraphBuilder.composable(
            "${route}/{$ACCOUNT_ID_KEY}",
            arguments = listOf(
                navArgument(ACCOUNT_ID_KEY) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString(ACCOUNT_ID_KEY)
            CreateAccountScreenScreen(
                modifier = modifier,
                accountId = accountId,
                onNavigateBack = navController::popBackStack,
            )
        }
    }
}