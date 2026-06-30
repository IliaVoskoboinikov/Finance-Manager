package soft.divan.financemanager.presenter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi

private const val ROOT_MAIN = "root_main"

@Composable
fun RootNavGraph(
    splashFeatureApi: SplashScreenFeatureApi,
    authFeatureApi: AuthFeatureApi,
    getAuthStatusUseCase: GetAuthStatusUseCase,
    mainScreen: @Composable () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val authStatus by getAuthStatusUseCase().collectAsState(initial = null)

    // Реактивно следим за статусом авторизации
    LaunchedEffect(authStatus) {
        if (authStatus == AuthStatus.UNAUTHORIZED) {
            // Если статус стал UNAUTHORIZED, очищаем весь стек и идем на экран авторизации
            navController.navigate(authFeatureApi.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = splashFeatureApi.route
    ) {
        splashFeatureApi.registerGraph(
            navGraphBuilder = this@NavHost,
            navController = navController,
            onFinish = {
                scope.launch {
                    val currentStatus = getAuthStatusUseCase().first()
                    if (currentStatus == AuthStatus.UNAUTHORIZED) {
                        navController.navigate(authFeatureApi.route) {
                            popUpTo(splashFeatureApi.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ROOT_MAIN) {
                            popUpTo(splashFeatureApi.route) { inclusive = true }
                        }
                    }
                }
            }
        )

        authFeatureApi.registerGraph(
            navGraphBuilder = this@NavHost,
            navController = navController,
            onFinish = {
                navController.navigate(ROOT_MAIN) {
                    popUpTo(authFeatureApi.route) { inclusive = true }
                }
            }
        )

        composable(ROOT_MAIN) {
            mainScreen()
        }
    }
}
