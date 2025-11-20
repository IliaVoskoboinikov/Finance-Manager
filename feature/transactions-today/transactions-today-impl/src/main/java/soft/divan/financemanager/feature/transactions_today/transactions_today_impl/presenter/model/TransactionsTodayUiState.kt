package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model

sealed interface TransactionsTodayUiState {
    data object Loading : TransactionsTodayUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : TransactionsTodayUiState

    data class Error(val message: String) : TransactionsTodayUiState
}