package soft.divan.financemanager.feature.design_app.design_app_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_impl.precenter.DesignAppScreen

import javax.inject.Inject

private const val baseRoute = "design_app"

class DesignAppFeatureImpl @Inject constructor() : DesignAppFeatureApi {

    override val route: String = baseRoute

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {

        navGraphBuilder.composable(route) {
            DesignAppScreen(
                modifier,
                navController = navController
            )
        }
    }


}