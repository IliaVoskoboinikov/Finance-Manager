package soft.divan.financemanager.core.featureapi

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface FeatureApi {
    val route: String

    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier = Modifier
    )
}
