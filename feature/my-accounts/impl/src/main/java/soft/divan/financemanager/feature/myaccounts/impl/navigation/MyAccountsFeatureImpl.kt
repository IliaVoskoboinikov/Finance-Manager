package soft.divan.financemanager.feature.myaccounts.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.account.api.AccountKey
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsKey
import soft.divan.financemanager.feature.myaccounts.impl.presenter.screens.MyAccountsScreen
import javax.inject.Inject

class MyAccountsFeatureImpl @Inject constructor() : MyAccountsFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<MyAccountsKey> {
            MyAccountsScreen(
                modifier = modifier,
                onNavigateToUpdateAccount = { accountId ->
                    navigator.goTo(AccountKey(accountId = accountId))
                },
                onNavigateToCreateAccount = {
                    navigator.goTo(AccountKey())
                }
            )
        }
    }
}
