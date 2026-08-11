package soft.divan.financemanager.feature.account.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import jakarta.inject.Inject
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.account.api.AccountKey
import soft.divan.financemanager.feature.account.impl.precenter.screens.AccountScreenScreen

class AccountFeatureImpl @Inject constructor() : AccountFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<AccountKey> { accountKey ->
            AccountScreenScreen(
                modifier = modifier,
                accountId = accountKey.accountId,
                onNavigateBack = navigator::back
            )
        }
    }
}
