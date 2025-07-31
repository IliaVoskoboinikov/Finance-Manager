package soft.divan.financemanager.feature.design_app.design_app_impl.precenter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetCustomAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetThemeModeUseCase
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

@HiltViewModel
class DesignAppViewModel @Inject constructor(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    val getAccentColorUseCase: GetAccentColorUseCase,
    val setAccentColorUseCase: SetAccentColorUseCase,
    private val setCustomAccentColorUseCase: SetCustomAccentColorUseCase
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode
        .onStart { getThemeMode() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ThemeMode.SYSTEM
        )

    private val _accentColor = MutableStateFlow(AccentColor.MINT)

    val accentColor = _accentColor
        .onStart { getAccentColor() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            AccentColor.MINT
        )

    private fun getThemeMode() {
        getThemeModeUseCase()
            .onStart {
                _themeMode.update { ThemeMode.SYSTEM }
            }
            .onEach { data ->
                _themeMode.update { data }
            }
            .catch { exception ->
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

    }

    private fun getAccentColor() {
        getAccentColorUseCase()
            .onStart {
                _accentColor.update { AccentColor.MINT }
            }
            .onEach { data ->
                _accentColor.update { data }
            }
            .catch { exception ->
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

    }

    fun setAccentColor(accentColor: AccentColor) {
        viewModelScope.launch(Dispatchers.IO) {
            setAccentColorUseCase(accentColor)
        }
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            setThemeModeUseCase(mode)
        }
    }

    fun setCustomColor(color: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            setCustomAccentColorUseCase(color)
            setAccentColor(AccentColor.CUSTOM)
        }
    }

}
