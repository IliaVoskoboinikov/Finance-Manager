package soft.divan.financemanager.feature.income.presenter.model

import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction

sealed interface IncomeUiState {
    data object Loading : IncomeUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : IncomeUiState

    data class Error(val message: String) : IncomeUiState
}