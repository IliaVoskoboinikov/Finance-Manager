package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model

import androidx.annotation.StringRes

sealed interface CreateAccountUiState {
    data object Loading : CreateAccountUiState
    data object Success : CreateAccountUiState
    data class Error(@field:StringRes val message: String) : CreateAccountUiState
}