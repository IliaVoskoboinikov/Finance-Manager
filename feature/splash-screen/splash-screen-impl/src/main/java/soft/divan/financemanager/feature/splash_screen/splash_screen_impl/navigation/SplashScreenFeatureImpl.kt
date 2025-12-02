package soft.divan.financemanager.feature.splash_screen.splash_screen_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_impl.screens.SplashScreen
import soft.divan.financemanager.feature.transactions_today.transactions_today_api.TransactionsTodayFeatureApi
import javax.inject.Inject


private const val BASE_ROUTE = "splash-screen"

class SplashScreenFeatureImpl @Inject constructor() : SplashScreenFeatureApi {

    override val route: String = BASE_ROUTE

    @Inject
    lateinit var transactionsTodayFeatureApi: TransactionsTodayFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            SplashScreen(
                onNavigateToExpenses = {
                    navController.navigate(transactionsTodayFeatureApi.route /*transactionsTodayRouteWithArgs(false)*/) {
                        popUpTo(route) { inclusive = true }
                    }
                },
            )
        }
    }
}