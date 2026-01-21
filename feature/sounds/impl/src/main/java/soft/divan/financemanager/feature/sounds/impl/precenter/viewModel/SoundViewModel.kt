package soft.divan.financemanager.feature.sounds.impl.precenter.viewModel

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
import soft.divan.financemanager.feature.sounds.impl.R
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.SetSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.precenter.model.SoundsUiState
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(
    private val setSoundsEnabledUseCase: SetSoundsEnabledUseCase,
    private val observeSoundsEnabledUseCase: ObserveSoundsEnabledUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SoundsUiState>(SoundsUiState.Loading)
    val uiState: StateFlow<SoundsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            observeSoundsEnabledUseCase()
                .onStart { _uiState.update { SoundsUiState.Loading } }
                .catch { _uiState.update { SoundsUiState.Error(R.string.error) } }
                .collect { isEnabled ->
                    _uiState.update { SoundsUiState.Success(isEnabled) }
                }
        }
    }

    fun setSoundsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setSoundsEnabledUseCase(isEnabled)
        }
    }


}