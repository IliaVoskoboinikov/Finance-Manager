package soft.divan.financemanager.feature.history.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String,
    ) : HistoryUiState

    data class Error(@field:StringRes val message: Int) : HistoryUiState
}