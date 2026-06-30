package soft.divan.financemanager.feature.auth.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.auth.impl.presenter.screen.AuthScreen
import soft.divan.financemanager.feature.auth.impl.presenter.screen.ProfileScreen
import javax.inject.Inject

private const val AUTH_ROUTE = "auth-screen"
private const val PROFILE_ROUTE = "profile-screen"

class AuthFeatureImpl @Inject constructor() : AuthFeatureApi {

    override val route: String = AUTH_ROUTE
    override val profileRoute: String = PROFILE_ROUTE

    /**
     * Используется для вложенной навигации (например, внутри Settings)
     */
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        // Профиль регистрируем как корень этого scope
        navGraphBuilder.composable(scope.route()) {
            ProfileScreen(
                onNavigateToAuth = {
                    navController.navigate(scope.route(AUTH_ROUTE))
                }
            )
        }

        // Авторизация остается дочерним роутом
        navGraphBuilder.composable(scope.route(AUTH_ROUTE)) {
            AuthScreen(
                onAuthSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }

    /**
     * Используется для корневой навигации (после SplashScreen)
     */
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier,
        onFinish: () -> Unit
    ) {
        navGraphBuilder.composable(route) {
            AuthScreen(
                onAuthSuccess = { onFinish() }
            )
        }

        navGraphBuilder.composable(profileRoute) {
            ProfileScreen(
                onNavigateToAuth = {
                    navController.navigate(route)
                }
            )
        }
    }
}
