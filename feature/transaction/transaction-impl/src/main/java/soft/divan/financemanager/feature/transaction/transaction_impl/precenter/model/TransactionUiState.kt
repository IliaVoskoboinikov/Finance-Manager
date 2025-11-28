package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionUiState {
    data object Loading : TransactionUiState
    data class Success(
        val transaction: UiTransaction,
        val categories: List<UiCategory>,
        val accounts: List<AccountUi>,
    ) : TransactionUiState

    data class Error(@field:StringRes val messageRes: Int) : TransactionUiState
}
