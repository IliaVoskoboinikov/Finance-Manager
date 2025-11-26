package soft.divan.financemanager.feature.create_account.create_account_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jakarta.inject.Inject
import soft.divan.financemanager.feature.create_account.create_account_api.CreateAccountFeatureApi
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.screens.CreateAccountScreenScreen

const val accountIdKey: String = "accountId"

class CreateAccountFeatureImpl @Inject constructor() : CreateAccountFeatureApi {

    override val route: String = "createAccount"

    override fun accountRouteWithArgs(accountId: Int?) =
        if (accountId == null) "$route/-1" else "$route/$accountId"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            "${route}/{$accountIdKey}",
            arguments = listOf(
                navArgument(accountIdKey) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getInt(accountIdKey)
            val accountId = if (rawId == -1) null else rawId


            CreateAccountScreenScreen(
                modifier = modifier,
                accountId = accountId,
                onNavigateBack = navController::popBackStack,
            )
        }
    }
}