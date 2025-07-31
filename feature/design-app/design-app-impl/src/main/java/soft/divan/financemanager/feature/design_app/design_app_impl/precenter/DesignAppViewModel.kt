package soft.divan.financemanager.feature.design_app.design_app_impl.precenter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetCustomAccentColorUseCase
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
    private val getCustomAccentColorUseCase: GetCustomAccentColorUseCase,
    private val setCustomAccentColorUseCase: SetCustomAccentColorUseCase
) : ViewModel() {

    val themeMode = getThemeModeUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM
    )
    val accentColor = getAccentColorUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccentColor.MINT
    )

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
