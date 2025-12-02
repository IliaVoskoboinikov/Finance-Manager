package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionsTodayUiState {
    data object Loading : TransactionsTodayUiState
    data class Success(
        val transactions: List<TransactionUi>,
        val sumTransaction: String
    ) : TransactionsTodayUiState

    data class Error(@field:StringRes val messageRes: Int) : TransactionsTodayUiState
}