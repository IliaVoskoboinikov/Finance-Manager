package soft.divan.financemanager.feature.haptic.haptic_impl.precenter.viewModel

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
import soft.divan.financemanager.feature.haptic.haptic_impl.R
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.ObserveHapticEnabledUseCase
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.SetHapticEnabledUseCase
import soft.divan.financemanager.feature.haptic.haptic_impl.precenter.model.HapticUiState
import javax.inject.Inject

@HiltViewModel
class HapticViewModel @Inject constructor(
    private val setHapticEnabledEnabledUseCase: SetHapticEnabledUseCase,
    private val observeHapticEnabledUseCase: ObserveHapticEnabledUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HapticUiState>(HapticUiState.Loading)
    val uiState: StateFlow<HapticUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            observeHapticEnabledUseCase.invoke()
                .onStart { _uiState.update { HapticUiState.Loading } }
                .catch { _uiState.update { HapticUiState.Error(R.string.error) } }
                .collect { isEnabled ->
                    _uiState.update { HapticUiState.Success(isEnabled) }
                }
        }
    }

    fun setHapticEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setHapticEnabledEnabledUseCase(isEnabled)
        }
    }


}