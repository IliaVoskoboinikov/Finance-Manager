package soft.divan.financemanager.feature.myaccounts.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.presenter.screens.MyAccountsScreen
import javax.inject.Inject

private const val baseRoute = "my_accounts"

class MyAccountsFeatureImpl @Inject constructor() : MyAccountsFeatureApi {

    override val route: String = baseRoute

    @Inject
    lateinit var accountFeatureApi: AccountFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(route) {
            MyAccountsScreen(
                modifier = modifier,
                onNavigateToUpdateAccount = { idAccount ->
                    navController.navigate(
                        accountFeatureApi.accountRouteWithArgs(
                            idAccount
                        )
                    )
                },
                onNavigateToCreateAccount = { navController.navigate(accountFeatureApi.route) }
            )
        }


    }
}