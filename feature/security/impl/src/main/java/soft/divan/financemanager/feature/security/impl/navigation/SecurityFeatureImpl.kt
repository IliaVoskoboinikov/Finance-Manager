package soft.divan.financemanager.feature.security.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.impl.presenter.screen.CreatePinScreen
import soft.divan.financemanager.feature.security.impl.presenter.screen.SecurityScreen
import javax.inject.Inject

private const val BASE_ROUTE = "security"
private const val SCENARIO_SECURITY_ROUTE = "${BASE_ROUTE}/scenario"
private const val SCREEN_CREATE_ROUTE = "$SCENARIO_SECURITY_ROUTE/create_pin"

class SecurityFeatureImpl @Inject constructor() : SecurityFeatureApi {

    override val route: String = BASE_ROUTE

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
                    navController.navigate(SCENARIO_SECURITY_ROUTE)
                },
            )
        }

        /* Nested graph for internal scenario */
        navGraphBuilder.navigation(
            route = SCENARIO_SECURITY_ROUTE,
            startDestination = SCREEN_CREATE_ROUTE
        ) {

            composable(route = SCREEN_CREATE_ROUTE) {
                CreatePinScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

        }

    }
}