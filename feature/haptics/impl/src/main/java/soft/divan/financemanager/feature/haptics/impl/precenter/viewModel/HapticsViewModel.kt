package soft.divan.financemanager.feature.haptics.impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.haptics.impl.R
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.SetHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.impl.precenter.model.HapticsUiState
import javax.inject.Inject

@HiltViewModel
class HapticsViewModel @Inject constructor(
    private val setHapticEnabledEnabledUseCase: SetHapticsEnabledUseCase,
    private val observeHapticsEnabledUseCase: ObserveHapticsEnabledUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HapticsUiState>(HapticsUiState.Loading)
    val uiState: StateFlow<HapticsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            observeHapticsEnabledUseCase()
                .onStart { _uiState.update { HapticsUiState.Loading } }
                .catch { _uiState.update { HapticsUiState.Error(R.string.error) } }
                .collect { isEnabled ->
                    _uiState.update { HapticsUiState.Success(isEnabled) }
                }
        }
    }

    fun setHapticEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setHapticEnabledEnabledUseCase(isEnabled)
        }
    }
}
