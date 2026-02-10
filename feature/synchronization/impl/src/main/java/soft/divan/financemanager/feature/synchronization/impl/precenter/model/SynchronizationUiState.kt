package soft.divan.financemanager.feature.synchronization.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface SynchronizationUiState {
    data object Loading : SynchronizationUiState
    data class Success(
        val lastSyncTime: String?,
        val hoursInterval: Int
    ) : SynchronizationUiState

    data class Error(@field:StringRes val messageRes: Int) : SynchronizationUiState
}
// Revue me>>
