package soft.divan.financemanager.feature.transaction.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.api.TransactionKey
import soft.divan.financemanager.feature.transaction.impl.precenter.screens.TransactionScreen
import javax.inject.Inject

class TransactionFeatureImpl @Inject constructor() : TransactionFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<TransactionKey> { transactionKey ->
            TransactionScreen(
                modifier = modifier,
                isIncome = transactionKey.isIncome,
                transactionId = transactionKey.transactionId,
                onNavigateBack = navigator::back
            )
        }
    }
}
