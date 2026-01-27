package soft.divan.financemanager.feature.splashscreen.api

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface SplashScreenFeatureApi {
    val route: String

    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier = Modifier,
        onFinish: () -> Unit
    )
}