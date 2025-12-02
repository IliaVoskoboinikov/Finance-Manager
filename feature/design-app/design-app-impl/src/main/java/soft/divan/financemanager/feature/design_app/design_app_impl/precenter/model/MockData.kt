package soft.divan.financemanager.feature.design_app.design_app_impl.precenter.model

import soft.divan.financemanager.feature.design_app.design_app_impl.R
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

val mockDesignUiStateSuccess = DesignUiState.Success(
    themeMode = ThemeMode.LIGHT,
    accentColor = AccentColor.BLUE
)

val mockDesignUiStateLoading = DesignUiState.Loading

val mockDesignUiStateError = DesignUiState.Error(
    messageRes = R.string.error
)