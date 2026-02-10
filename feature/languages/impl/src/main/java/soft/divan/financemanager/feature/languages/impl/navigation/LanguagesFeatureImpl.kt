package soft.divan.financemanager.feature.languages.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.languages.impl.precenter.screen.LanguagesScreen
import javax.inject.Inject

private const val BASE_ROUTE = "languages"

class LanguagesFeatureImpl @Inject constructor() : LanguagesFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            LanguagesScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}
// Revue me>>
