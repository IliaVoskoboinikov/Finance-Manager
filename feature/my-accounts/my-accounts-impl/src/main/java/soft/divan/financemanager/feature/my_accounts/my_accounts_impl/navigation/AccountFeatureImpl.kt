package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import soft.divan.financemanager.feature.create_account.create_account_api.CreateAccountFeatureApi
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.AccountFeatureApi
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.screens.AccountScreen
import javax.inject.Inject

private const val baseRoute = "account"

class AccountFeatureImpl @Inject constructor() : AccountFeatureApi {

    override val route: String = baseRoute

    @Inject
    lateinit var createAccountFeatureApi: CreateAccountFeatureApi

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
                        createAccountFeatureApi.accountRouteWithArgs(
                            idAccount
                        )
                    )
                },
                onNavigateToCreateAccount = { navController.navigate(createAccountFeatureApi.accountRouteWithArgs()) }
            )
        }


    }
}