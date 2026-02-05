package soft.divan.financemanager.feature.languages.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface LanguageUiState {
    data object Loading : LanguageUiState
    data class Success(val language: LanguageUi) : LanguageUiState
    data class Error(@field:StringRes val messageRes: Int) : LanguageUiState
}
