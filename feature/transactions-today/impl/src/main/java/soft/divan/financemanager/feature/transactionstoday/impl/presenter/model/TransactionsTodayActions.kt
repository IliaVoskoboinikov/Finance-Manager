package soft.divan.financemanager.feature.transactionstoday.impl.presenter.model

data class TransactionsTodayActions(
    val onRetry: () -> Unit,
    val onNavigateToHistory: () -> Unit,
    val onNavigateToNewTransaction: () -> Unit,
    val onNavigateToOldTransaction: (String) -> Unit,
    val onHaptic: () -> Unit
)
