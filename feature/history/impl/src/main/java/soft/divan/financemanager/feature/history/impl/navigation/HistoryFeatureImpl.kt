package soft.divan.financemanager.feature.history.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.analysis.api.AnalysisKey
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.history.api.HistoryKey
import soft.divan.financemanager.feature.history.impl.precenter.screens.HistoryScreen
import soft.divan.financemanager.feature.transaction.api.TransactionKey
import javax.inject.Inject

class HistoryFeatureImpl @Inject constructor() : HistoryFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<HistoryKey> { historyKey ->
            val isIncome = historyKey.isIncome

            HistoryScreen(
                modifier = modifier,
                isIncome = isIncome,
                onNavigateBack = navigator::back,
                onNavigateToTransaction = { transactionId ->
                    navigator.goTo(
                        TransactionKey(isIncome = isIncome, transactionId = transactionId)
                    )
                },
                onNavigateToAnalysis = {
                    navigator.goTo(AnalysisKey(isIncome = isIncome))
                }
            )
        }
    }
}
