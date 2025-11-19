package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper

import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction

sealed interface TransactionsTodayUiState {
    data object Loading : TransactionsTodayUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : TransactionsTodayUiState

    data class Error(val message: String) : TransactionsTodayUiState
}