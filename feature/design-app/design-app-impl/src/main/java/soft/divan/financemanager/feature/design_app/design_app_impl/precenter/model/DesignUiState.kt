package soft.divan.financemanager.feature.design_app.design_app_impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

@Immutable
sealed interface DesignUiState {
    data object Loading : DesignUiState
    data class Success(
        val themeMode: ThemeMode,
        val accentColor: AccentColor
    ) : DesignUiState

    data class Error(@field:StringRes val messageRes: Int) : DesignUiState
}