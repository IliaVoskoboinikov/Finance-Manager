package soft.divan.financemanager.feature.designapp.impl.precenter.model

import soft.divan.financemanager.feature.design_app.impl.R
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

val mockDesignUiStateSuccess = DesignUiState.Success(
    themeMode = ThemeMode.LIGHT,
    accentColor = AccentColor.BLUE
)

val mockDesignUiStateLoading = DesignUiState.Loading

val mockDesignUiStateError = DesignUiState.Error(
    messageRes = R.string.error
)
// Revue me>>
