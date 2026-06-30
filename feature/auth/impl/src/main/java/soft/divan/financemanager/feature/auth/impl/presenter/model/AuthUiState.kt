package soft.divan.financemanager.feature.auth.impl.presenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface AuthUiState {
    data object Loading : AuthUiState

    data class Success(
        val authUi: AuthUi = AuthUi(),
        val isSyncing: Boolean = false,
        val showMergeDialog: Boolean = false,
        val showLogoutDialog: Boolean = false,
        @field:StringRes val errorMessage: Int? = null
    ) : AuthUiState

    data class Error(@field:StringRes val message: Int) : AuthUiState
}
