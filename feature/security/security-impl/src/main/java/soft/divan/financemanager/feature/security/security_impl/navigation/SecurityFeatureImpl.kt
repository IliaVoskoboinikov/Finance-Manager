package soft.divan.financemanager.feature.security.security_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.security_impl.presenter.screen.SecurityScreen
import javax.inject.Inject

private const val baseRoute = "security"
private const val scenarioSecurityRoute = "${baseRoute}/scenario"
private const val screenSecurityHistoryRoute = "$scenarioSecurityRoute/income_history"

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
            )
        }

    }
}