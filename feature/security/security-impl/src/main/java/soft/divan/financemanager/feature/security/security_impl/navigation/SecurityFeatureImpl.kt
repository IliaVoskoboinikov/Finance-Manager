package soft.divan.financemanager.feature.security.security_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.security_impl.presenter.screen.CreatePinScreen
import soft.divan.financemanager.feature.security.security_impl.presenter.screen.SecurityScreen
import javax.inject.Inject

private const val baseRoute = "security"
private const val scenarioSecurityRoute = "${baseRoute}/scenario"
private const val screenScreenCreateRoute = "$scenarioSecurityRoute/create_pin"

class SecurityFeatureImpl @Inject constructor() : SecurityFeatureApi {

    override val route: String = baseRoute


    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            SecurityScreen(
                modifier = modifier,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreatePin = {
                    navController.navigate(scenarioSecurityRoute)
                },
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = scenarioSecurityRoute,
            startDestination = screenScreenCreateRoute
        ) {

            composable(route = screenScreenCreateRoute) {
                CreatePinScreen(
                    onNavegateBack = {
                        navController.popBackStack()
                    }
                )
            }

        }

    }
}