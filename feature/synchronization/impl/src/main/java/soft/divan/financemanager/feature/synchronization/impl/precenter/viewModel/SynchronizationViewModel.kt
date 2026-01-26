package soft.divan.financemanager.feature.synchronization.impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.domain.data.DateHelper.toDateTimeString
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.feature.synchronization.impl.R
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.ObserveLastSyncTimeUseCase
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.SetSyncIntervalHoursUseCase
import soft.divan.financemanager.feature.synchronization.impl.precenter.model.SynchronizationUiState
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.worker.BASE_SYNCHRONIZATION_PERIOD_IN_HOURS
import javax.inject.Inject

@HiltViewModel
class SynchronizationViewModel @Inject constructor(
    private val setSyncIntervalHoursUseCase: SetSyncIntervalHoursUseCase,
    observeLastSyncTimeUseCase: ObserveLastSyncTimeUseCase,
    observeSyncIntervalHoursUseCase: ObserveSyncIntervalHoursUseCase,
) : ViewModel() {

    val uiState: StateFlow<SynchronizationUiState> =
        combine(
            observeLastSyncTimeUseCase(),
            observeSyncIntervalHoursUseCase()
        ) { lastSyncTime, interval ->
            SynchronizationUiState.Success(
                lastSyncTime = lastSyncTime?.toDateTimeString(),
                hoursInterval = interval ?: BASE_SYNCHRONIZATION_PERIOD_IN_HOURS
            ) as SynchronizationUiState
        }
            .catch {
                emit(SynchronizationUiState.Error(R.string.error))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
                initialValue = SynchronizationUiState.Loading
            )

    fun onIntervalChanged(interval: Int) {
        viewModelScope.launch {
            setSyncIntervalHoursUseCase(interval)
        }
    }
}
