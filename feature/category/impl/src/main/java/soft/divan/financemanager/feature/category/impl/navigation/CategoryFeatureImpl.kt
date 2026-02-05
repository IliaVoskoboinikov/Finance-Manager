package soft.divan.financemanager.feature.category.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.impl.presenter.screen.CategoriesScreen
import javax.inject.Inject

private const val BASE_ROUTE = "category"

class CategoryFeatureImpl @Inject constructor() : CategoryFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            CategoriesScreen(
                modifier = modifier
            )
        }
    }
}
