package soft.divan.financemanager.feature.haptics.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface HapticsUiState {
    data object Loading : HapticsUiState
    data class Success(
        val isEnabled: Boolean,
    ) : HapticsUiState

    data class Error(@field:StringRes val messageRes: Int) : HapticsUiState
}