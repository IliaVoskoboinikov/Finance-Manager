package soft.divan.financemanager.feature.synchronization.synchronization_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.synchronization.synchronization_api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.synchronization.synchronization_impl.precenter.screen.SynchronizationScreen
import javax.inject.Inject

private const val BASE_ROUTE = "synchronization"

class SynchronizationFeatureImpl @Inject constructor() : SynchronizationFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            SynchronizationScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}