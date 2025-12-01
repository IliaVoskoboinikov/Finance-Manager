package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.account.account_api.AccountFeatureApi
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.screens.AccountScreen
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
            AccountScreen(
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