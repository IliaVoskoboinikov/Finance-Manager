package soft.divan.financemanager.feature.sounds.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface SoundsUiState {
    data object Loading : SoundsUiState
    data class Success(
        val isEnabled: Boolean,
    ) : SoundsUiState

    data class Error(@field:StringRes val messageRes: Int) : SoundsUiState
}