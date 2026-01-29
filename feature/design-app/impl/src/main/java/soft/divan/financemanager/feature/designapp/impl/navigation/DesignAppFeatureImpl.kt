package soft.divan.financemanager.feature.designapp.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.designapp.api.DesignAppFeatureApi
import soft.divan.financemanager.feature.designapp.impl.precenter.screen.DesignAppScreen

import javax.inject.Inject

private const val BASE_ROUTE = "design_app"

class DesignAppFeatureImpl @Inject constructor() : DesignAppFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {

        navGraphBuilder.composable(scope.route()) {
            DesignAppScreen(
                modifier = modifier,
            )
        }
    }
}
