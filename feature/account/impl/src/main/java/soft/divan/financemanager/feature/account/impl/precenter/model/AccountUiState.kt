package soft.divan.financemanager.feature.account.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountUiState {
    data object Loading : AccountUiState

    data class Success(
        val account: AccountUi,
        val mode: AccountMode,
        /**
         * Есть ли у счёта операции. В режиме редактирования определяет текст диалога удаления:
         * счёт с операциями будет заархивирован, пустой — удалён физически. В режиме создания
         * не используется.
         */
        val hasTransactions: Boolean = false
    ) : AccountUiState

    data class Error(@field:StringRes val message: Int) : AccountUiState
}
