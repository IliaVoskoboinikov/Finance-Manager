package soft.divan.financemanager.feature.create_account.create_account_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import jakarta.inject.Inject
import soft.divan.financemanager.feature.create_account.create_account_api.CreateAccountFeatureApi
import soft.divan.financemanager.feature.create_account.create_account_impl.precenter.screens.CreateAccountScreenScreen

class CreateAccountFeatureImpl @Inject constructor() : CreateAccountFeatureApi {

    override val route: String = "createAccount"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            CreateAccountScreenScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}