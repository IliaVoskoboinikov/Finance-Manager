package soft.divan.financemanager.feature.account.account_impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountUiState {
    data object Loading : AccountUiState

    data class Success(
        val account: AccountUiModel,
        val mode: AccountMode
    ) : AccountUiState

    data class Error(@field:StringRes val message: Int) : AccountUiState
}