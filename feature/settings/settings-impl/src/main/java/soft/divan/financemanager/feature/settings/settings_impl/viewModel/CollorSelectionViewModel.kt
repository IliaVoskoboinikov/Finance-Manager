package soft.divan.financemanager.feature.settings.settings_impl.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

@HiltViewModel
class CollorSelectionViewModel @Inject constructor(
    val getAccentColorUseCase: GetAccentColorUseCase,
    val setAccentColorUseCase: SetAccentColorUseCase,
) : ViewModel() {

    val accentColor = getAccentColorUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccentColor.MINT
    )

    fun setAccentColor(accentColor: AccentColor) {
        viewModelScope.launch {
            setAccentColorUseCase(accentColor)
        }
    }
}
