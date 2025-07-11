package soft.divan.financemanager.category.presenter.model

import soft.divan.financemanager.feature.expenses_income_shared.presenter.model.UiCategory

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
    data class Success(
        val categories: List<UiCategory>,
        val sortedCategories: List<UiCategory>
    ) : CategoriesUiState

}
