package soft.divan.financemanager.feature.category.category_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.category_impl.presenter.screen.CategoriesScreen
import javax.inject.Inject

private const val baseRoute = "category"
private const val scenarioCategoryRoute = "${baseRoute}/scenario"
private const val screenCategoryHistoryRoute = "$scenarioCategoryRoute/income_history"

class CategoryFeatureImpl @Inject constructor() : CategoryFeatureApi {

    override val categoryRoute: String = baseRoute

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(categoryRoute) {
            CategoriesScreen(
                modifier = modifier,
                navController = navController,
            )
        }


    }
}