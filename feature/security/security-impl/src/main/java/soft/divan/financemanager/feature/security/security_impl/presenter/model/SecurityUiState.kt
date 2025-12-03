package soft.divan.financemanager.feature.security.security_impl.presenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface SecurityUiState {
    data object Loading : SecurityUiState
    data class Success(
        val pin: String = "",
        val hasPin: Boolean = false
    ) : SecurityUiState

    data class Error(@field:StringRes val messageRes: Int) : SecurityUiState
}
