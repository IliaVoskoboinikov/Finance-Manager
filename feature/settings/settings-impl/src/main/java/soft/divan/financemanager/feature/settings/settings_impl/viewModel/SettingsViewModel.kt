package soft.divan.financemanager.feature.settings.settings_impl.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.SetThemeModeUseCase
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase
) : ViewModel() {

    val themeMode = getThemeModeUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.LIGHT
    )

    val isDarkThemeEnabled = themeMode.map { it == ThemeMode.DARK }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleTheme(isDark: Boolean) {
        val newMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
        onThemeSelected(newMode)
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(mode)
        }
    }
}
