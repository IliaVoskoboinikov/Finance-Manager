package soft.divan.financemanager.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.Const.DEFAULT_STOP_TIMEOUT_MS
import soft.divan.financemanager.core.network.util.NetworkMonitor
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
    private val hapticsManager: HapticsManager,
    @param:IoDispatcher ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MS),
            initialValue = false
        )

    fun hapticToggleMenu() {
        hapticsManager.perform(HapticType.TOGGLE)
    }
}
