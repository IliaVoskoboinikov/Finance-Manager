package soft.divan.financemanager.feature.category.impl.presenter.model

val mockCategories = listOf(
    UiCategory(
        id = "1",
        name = "Salary",
        emoji = "💰",
        isIncome = true
    ),
    UiCategory(
        id = "2",
        name = "Gift",
        emoji = "🎁",
        isIncome = true
    ),
    UiCategory(
        id = "3",
        name = "Food",
        emoji = "🍕",
        isIncome = false
    ),
    UiCategory(
        id = "4",
        name = "Rent",
        emoji = "🏠",
        isIncome = false
    ),
    UiCategory(
        id = "5",
        name = "Transport",
        emoji = "🚌",
        isIncome = false
    )
)

val mockCategoriesUiStateSuccess = CategoriesUiState.Success(
    categories = mockCategories,
    filteredCategories = mockCategories
)
