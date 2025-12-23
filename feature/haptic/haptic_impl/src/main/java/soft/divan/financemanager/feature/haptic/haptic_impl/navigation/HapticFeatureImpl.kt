package soft.divan.financemanager.feature.haptic.haptic_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.haptic.haptic_api.HapticFeatureApi
import soft.divan.financemanager.feature.haptic.haptic_impl.precenter.screen.HapticScreen
import javax.inject.Inject

private const val BASE_ROUTE = "haptic"

class HapticFeatureImpl @Inject constructor() : HapticFeatureApi {

    override val route: String = BASE_ROUTE

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            HapticScreen(
                modifier = modifier,
                onNavigateBack = navController::popBackStack
            )
        }
    }


}