package soft.divan.financemanager.feature.transactionstoday.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.history.api.HistoryKey
import soft.divan.financemanager.feature.transaction.api.TransactionKey
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayKey
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.screen.TransactionsTodayScreen
import javax.inject.Inject

class TransactionsTodayFeatureImpl @Inject constructor() : TransactionsTodayFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<TransactionsTodayKey> { todayKey ->
            val isIncome = todayKey.isIncome

            TransactionsTodayScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateToHistory = {
                    navigator.goTo(HistoryKey(isIncome = isIncome))
                },
                onNavigateToNewTransaction = {
                    navigator.goTo(TransactionKey(isIncome = isIncome))
                },
                onNavigateToOldTransaction = { transactionId ->
                    navigator.goTo(
                        TransactionKey(isIncome = isIncome, transactionId = transactionId)
                    )
                }
            )
        }
    }
}
