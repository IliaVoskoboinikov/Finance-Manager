package soft.divan.financemanager.presenter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi

private const val ROOT_MAIN = "root_main"

@Composable
fun RootNavGraph(
    splashFeatureApi: SplashScreenFeatureApi,
    mainScreen: @Composable () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = splashFeatureApi.route
    ) {
        splashFeatureApi.registerGraph(
            navGraphBuilder = this@NavHost,
            navController = navController,
            onFinish = {
                navController.navigate(ROOT_MAIN) {
                    popUpTo(splashFeatureApi.route) {
                        inclusive = true
                    }
                }
            }
        )

        composable(ROOT_MAIN) {
            mainScreen()
        }
    }
}
// Revue me>>
