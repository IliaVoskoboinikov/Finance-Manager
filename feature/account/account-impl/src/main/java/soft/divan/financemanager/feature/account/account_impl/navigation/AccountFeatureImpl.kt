package soft.divan.financemanager.feature.account.account_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.account.account_impl.presenter.screens.AccountScreen

import javax.inject.Inject

private const val baseRoute = "account"
private const val scenarioAccountRoute = "${baseRoute}/scenario"
private const val screenAccountHistoryRoute = "$scenarioAccountRoute/income_history"

class AccountFeatureImpl @Inject constructor() : AccountFeatureApi {

    override val accountRoute: String = baseRoute

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(accountRoute) {
            AccountScreen(
                modifier = modifier,
                navController = navController,
            )
        }


    }
}