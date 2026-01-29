package soft.divan.financemanager.feature.synchronization.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.synchronization.impl.precenter.screen.SynchronizationScreen
import javax.inject.Inject

private const val BASE_ROUTE = "synchronization"

class SynchronizationFeatureImpl @Inject constructor() : SynchronizationFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            SynchronizationScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}