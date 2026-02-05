package soft.divan.financemanager.feature.myaccounts.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.core.featureapi.RouteScope
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.presenter.screens.MyAccountsScreen
import javax.inject.Inject

private const val BASE_ROUTE = "my_accounts"

class MyAccountsFeatureImpl @Inject constructor() : MyAccountsFeatureApi {

    override val route: String = BASE_ROUTE

    @Inject
    lateinit var accountFeatureApi: AccountFeatureApi

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        scope: RouteScope,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(scope.route()) {
            MyAccountsScreen(
                modifier = modifier,
                onNavigateToUpdateAccount = { idAccount ->
                    navController.navigate(
                        scope.route(
                            accountFeatureApi.accountRouteWithArgs(
                                idAccount
                            )
                        )
                    )
                },
                onNavigateToCreateAccount = {
                    navController.navigate(
                        scope.route(accountFeatureApi.route)
                    )
                }
            )
        }
        accountFeatureApi.registerGraph(
            navGraphBuilder = navGraphBuilder,
            navController = navController,
            scope = scope.child(accountFeatureApi.route),
            modifier = modifier
        )
    }
}
