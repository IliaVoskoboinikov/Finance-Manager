package soft.divan.financemanager.feature.category.category_impl.presenter.model

import soft.divan.financemanager.feature.category.category_impl.R

val mockCategories = listOf(
    UiCategory(
        id = 1,
        name = "Продукты",
        emoji = "🛒",
        isIncome = false
    ),
    UiCategory(
        id = 2,
        name = "Транспорт",
        emoji = "🚌",
        isIncome = false
    ),
    UiCategory(
        id = 3,
        name = "Развлечения",
        emoji = "🎮",
        isIncome = false
    ),
    UiCategory(
        id = 4,
        name = "Зарплата",
        emoji = "💰",
        isIncome = true
    ),
    UiCategory(
        id = 5,
        name = "Подарки",
        emoji = "🎁",
        isIncome = true
    )
)

val mockCategoriesUiStateError = CategoriesUiState.Error(
    message = R.string.error_loading
)

val mockCategoriesUiStateLoading = CategoriesUiState.Loading

val mockCategoriesUiStateSuccess = CategoriesUiState.Success(
    categories = mockCategories,
    filteredCategories = mockCategories,
)
