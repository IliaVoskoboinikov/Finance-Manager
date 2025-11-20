package soft.divan.financemanager.feature.category.category_impl.presenter.model

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
    data class Success(
        val categories: List<UiCategory>,
        val sortedCategories: List<UiCategory>
    ) : CategoriesUiState

}
//todo
