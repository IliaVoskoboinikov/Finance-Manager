package soft.divan.financemanager.feature.category.impl.presenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState

    data class Success(
        val categories: List<UiCategory>,
        val filteredCategories: List<UiCategory>
    ) : CategoriesUiState

    data class Error(@field:StringRes val message: Int) : CategoriesUiState

    data object EmptyData : CategoriesUiState
}
// Revue me>>
