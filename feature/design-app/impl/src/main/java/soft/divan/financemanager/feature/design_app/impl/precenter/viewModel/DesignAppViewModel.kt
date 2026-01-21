package soft.divan.financemanager.feature.design_app.impl.precenter.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.design_app.impl.R
import soft.divan.financemanager.feature.design_app.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.SetCustomAccentColorUseCase
import soft.divan.financemanager.feature.design_app.impl.domain.usecase.SetThemeModeUseCase
import soft.divan.financemanager.feature.design_app.impl.precenter.model.DesignUiState
import soft.divan.financemanager.feature.haptics.api.domain.HapticType
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

@HiltViewModel
class DesignAppViewModel @Inject constructor(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val getAccentColorUseCase: GetAccentColorUseCase,
    private val setAccentColorUseCase: SetAccentColorUseCase,
    private val setCustomAccentColorUseCase: SetCustomAccentColorUseCase,
    private val hapticsManager: HapticsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DesignUiState>(DesignUiState.Loading)
    val uiState: StateFlow<DesignUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        combine(getThemeModeUseCase(), getAccentColorUseCase())
        { themeMode, accentColor ->
            _uiState.update {
                DesignUiState.Success(
                    themeMode = themeMode,
                    accentColor = accentColor
                )
            }
        }
            .catch { _uiState.update { DesignUiState.Error(R.string.error) } }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun setAccentColor(accentColor: AccentColor) {
        hapticsManager.perform(HapticType.CLICK)
        viewModelScope.launch {
            setAccentColorUseCase(accentColor)
        }
    }

    fun onThemeSelected(mode: ThemeMode) {
        hapticsManager.perform(HapticType.CLICK)
        viewModelScope.launch {
            setThemeModeUseCase(mode)
        }
    }

    fun setCustomColor(color: Color) {
        hapticsManager.perform(HapticType.CLICK)
        viewModelScope.launch {
            setCustomAccentColorUseCase(color)
            setAccentColor(AccentColor.CUSTOM)
        }
    }

}