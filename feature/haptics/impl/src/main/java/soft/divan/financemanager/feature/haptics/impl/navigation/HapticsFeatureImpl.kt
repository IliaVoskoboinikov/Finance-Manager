package soft.divan.financemanager.feature.haptics.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.haptics.api.HapticsFeatureApi
import soft.divan.financemanager.feature.haptics.impl.precenter.screen.HapticsScreen
import javax.inject.Inject

private const val BASE_ROUTE = "haptics"

class HapticFeatureImpl @Inject constructor() : HapticsFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            HapticsScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }


}