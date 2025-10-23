package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

import androidx.annotation.StringRes
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiCategory
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction

sealed interface TransactionUiState {
    data object Loading : TransactionUiState
    data class Success(
        val transaction: UiTransaction,
        val categories: List<UiCategory>,
        val accountName: String,
    ) : TransactionUiState

    data class Error(val message: String) : TransactionUiState
}

sealed interface TransactionEvent {
    data object TransactionDeleted : TransactionEvent
    data object TransactionSaved : TransactionEvent
    data class ShowError(@StringRes val messageRes: Int) : TransactionEvent
}