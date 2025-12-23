package soft.divan.financemanager.feature.haptic.haptic_impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface HapticUiState {
    data object Loading : HapticUiState
    data class Success(
        val isEnabled: Boolean,
    ) : HapticUiState

    data class Error(@field:StringRes val messageRes: Int) : HapticUiState
}